package com.example.sms.ui.schedule;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sms.Login;
import com.example.sms.NavigationItemSelected;
import com.example.sms.R;
import com.example.sms.api.AuthApi;
import com.example.sms.api.StudentApi;
import com.example.sms.api.SubjectApi;
import com.example.sms.models.Student;
import com.example.sms.models.Subject;
import com.example.sms.ui.subject.SubjectAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ThoiKhoaBieuActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thoi_khoa_bieu);
        getSupportActionBar().setTitle("Thời Khóa Biểu");

        authApi = new AuthApi();

        drawerLayout = findViewById(R.id.thoi_khoa_bieu_drawer_layout);
        navigationView = findViewById(R.id.nav_view_schedule);

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
                    startActivity(new Intent(ThoiKhoaBieuActivity.this, Login.class));
                    finish();
                })
                .setNegativeButton("Không", null)
                .create();

        NavigationItemSelected navHelper = new NavigationItemSelected(navigationView, drawerLayout, logoutDialog, this);
        navHelper.itemSelected();

        RecyclerView rvSchedule = findViewById(R.id.rv_schedule);
        TextView tvEmpty = findViewById(R.id.tv_empty_schedule);
        TextView tvTitle = findViewById(R.id.tv_schedule_title);
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        
        SubjectAdapter adapter = new SubjectAdapter();
        rvSchedule.setAdapter(adapter);

        if (AuthApi.isAdmin) {
            tvTitle.setText("Tài khoản Admin không có thời khóa biểu");
            tvEmpty.setVisibility(View.GONE);
            rvSchedule.setVisibility(View.GONE);
        } else {
            FirebaseUser user = authApi.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                String userEmail = user.getEmail();
                StudentApi studentApi = new StudentApi();
                SubjectApi subjectApi = new SubjectApi();

                studentApi.getAllStudents(new com.example.sms.api.ApiCallback<List<Student>>() {
                    @Override
                    public void onSuccess(List<Student> students) {
                        String matchedStudentId = null;
                        for (Student s : students) {
                            if (s.getEmail() != null && s.getEmail().equalsIgnoreCase(userEmail)) {
                                matchedStudentId = s.getId();
                                break;
                            }
                        }

                        final String finalMatchedStudentId = matchedStudentId;

                        if (finalMatchedStudentId != null) {
                            subjectApi.getSubjectsForStudent(finalMatchedStudentId, new com.example.sms.api.ApiCallback<List<Subject>>() {
                                @Override
                                public void onSuccess(List<Subject> subjects) {
                                    if (subjects.isEmpty()) {
                                        tvEmpty.setVisibility(View.VISIBLE);
                                        rvSchedule.setVisibility(View.GONE);
                                        tvEmpty.setText("Không có môn học nào");
                                    } else {
                                        tvEmpty.setVisibility(View.GONE);
                                        rvSchedule.setVisibility(View.VISIBLE);
                                        adapter.setSubjects(subjects);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(ThoiKhoaBieuActivity.this, "Lỗi tải TKB", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            tvTitle.setText("Không tìm thấy thông tin sinh viên liên kết với tài khoản này.");
                            tvEmpty.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ThoiKhoaBieuActivity.this, "Lỗi xác thực", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
