package com.example.sms.ui.student;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sms.Login;
import com.example.sms.NavigationItemSelected;
import com.example.sms.R;
import com.example.sms.api.ApiCallback;
import com.example.sms.api.AuthApi;
import com.example.sms.api.StudentApi;
import com.example.sms.models.Student;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class SinhVienMainActivity extends AppCompatActivity {
    private AuthApi authApi;
    private StudentApi studentApi;

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private SearchView searchView;
    private FloatingActionButton fabAdd;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sinh_vien_main);
        
        getSupportActionBar().setTitle("Danh sách Sinh viên");

        authApi = new AuthApi();
        studentApi = new StudentApi();

        recyclerView = findViewById(R.id.recycler_view_students);
        searchView = findViewById(R.id.searchView);
        fabAdd = findViewById(R.id.fab_add_student);
        drawerLayout = findViewById(R.id.sinh_vien_drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Setup Drawer Menu
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        AlertDialog logoutDialog = new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Có", (dialog, which) -> {
                    authApi.logout();
                    startActivity(new Intent(SinhVienMainActivity.this, Login.class));
                    finish();
                })
                .setNegativeButton("Không", null)
                .create();

        NavigationItemSelected navHelper = new NavigationItemSelected(navigationView, drawerLayout, logoutDialog, this);
        navHelper.itemSelected();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter();
        recyclerView.setAdapter(adapter);

        // Kiểm tra quyền Admin
        authApi.checkAdminStatus(new ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isAdmin) {
                if (isAdmin) {
                    fabAdd.setVisibility(View.VISIBLE);
                } else {
                    fabAdd.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                fabAdd.setVisibility(View.GONE);
            }
        });

        // Load data
        loadStudents();

        // Search logic
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });

        // Add logic
        fabAdd.setOnClickListener(v -> showAddStudentDialog());
    }

    private void loadStudents() {
        studentApi.getAllStudents(new ApiCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> result) {
                adapter.setStudents(result);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SinhVienMainActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddStudentDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        EditText etTen = dialogView.findViewById(R.id.et_ten);
        EditText etMssv = dialogView.findViewById(R.id.et_mssv);
        EditText etEmail = dialogView.findViewById(R.id.et_email);
        EditText etLop = dialogView.findViewById(R.id.et_lop);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnAdd.setOnClickListener(v -> {
            String ten = etTen.getText().toString().trim();
            String mssv = etMssv.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String lop = etLop.getText().toString().trim();

            if (ten.isEmpty() || mssv.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và MSSV", Toast.LENGTH_SHORT).show();
                return;
            }

            Student newStudent = new Student("", ten, mssv, email, lop);
            studentApi.addStudent(newStudent, new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    if (email != null && !email.isEmpty()) {
                        // Create Auth Account automatically using secondary FirebaseApp instance to avoid signing out Admin
                        try {
                            FirebaseApp defaultApp = FirebaseApp.getInstance();
                            FirebaseOptions options = defaultApp.getOptions();
                            
                            // Check if temp app already exists, if so delete it first
                            try {
                                FirebaseApp.getInstance("temp_auth_app").delete();
                            } catch (Exception e) {}

                            FirebaseApp tempApp = FirebaseApp.initializeApp(SinhVienMainActivity.this, options, "temp_auth_app");
                            FirebaseAuth tempAuth = FirebaseAuth.getInstance(tempApp);
                            
                            // Default password is "123456"
                            tempAuth.createUserWithEmailAndPassword(email, "123456")
                                .addOnCompleteListener(task -> {
                                    tempApp.delete();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SinhVienMainActivity.this, "Đã thêm sinh viên và cấp tài khoản đăng nhập (mật khẩu mặc định: 123456)", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(SinhVienMainActivity.this, "Đã thêm sinh viên vào Database, nhưng không tạo được tài khoản Auth: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                        } catch (Exception e) {
                            Toast.makeText(SinhVienMainActivity.this, "Thêm sinh viên thành công. Không thể tự tạo tài khoản Auth.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(SinhVienMainActivity.this, "Thêm thành công (Chưa cấp tài khoản do thiếu Email)", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(SinhVienMainActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
