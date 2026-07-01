package com.example.sms.ui.subject;

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
import com.example.sms.api.SubjectApi;
import com.example.sms.models.Subject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class MonHocMainActivity extends AppCompatActivity {
    private AuthApi authApi;
    private SubjectApi subjectApi;

    private RecyclerView recyclerView;
    private SubjectAdapter adapter;
    private SearchView searchView;
    private FloatingActionButton fabAdd;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mon_hoc_main);
        
        getSupportActionBar().setTitle("Danh sách Môn học");

        authApi = new AuthApi();
        subjectApi = new SubjectApi();

        recyclerView = findViewById(R.id.recycler_view_subjects);
        searchView = findViewById(R.id.searchViewSubject);
        fabAdd = findViewById(R.id.fab_add_subject);
        drawerLayout = findViewById(R.id.mon_hoc_drawer_layout);
        navigationView = findViewById(R.id.nav_view_subject);

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
                    startActivity(new Intent(MonHocMainActivity.this, Login.class));
                    finish();
                })
                .setNegativeButton("Không", null)
                .create();

        NavigationItemSelected navHelper = new NavigationItemSelected(navigationView, drawerLayout, logoutDialog, this);
        navHelper.itemSelected();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubjectAdapter();
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
        loadSubjects();

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
        fabAdd.setOnClickListener(v -> showAddSubjectDialog());
    }

    private void loadSubjects() {
        subjectApi.getAllSubjects(new ApiCallback<List<Subject>>() {
            @Override
            public void onSuccess(List<Subject> result) {
                adapter.setSubjects(result);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MonHocMainActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddSubjectDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_subject, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        EditText etTenMon = dialogView.findViewById(R.id.et_ten_mon);
        EditText etGiangVien = dialogView.findViewById(R.id.et_giang_vien);
        EditText etCaHoc = dialogView.findViewById(R.id.et_ca_hoc);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnAdd.setOnClickListener(v -> {
            String tenMon = etTenMon.getText().toString().trim();
            String giangVien = etGiangVien.getText().toString().trim();
            String caHoc = etCaHoc.getText().toString().trim();

            if (tenMon.isEmpty() || caHoc.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên môn và ca học", Toast.LENGTH_SHORT).show();
                return;
            }

            Subject newSubject = new Subject("", tenMon, giangVien, caHoc);
            subjectApi.addSubject(newSubject, new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(MonHocMainActivity.this, "Thêm môn học thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(MonHocMainActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
