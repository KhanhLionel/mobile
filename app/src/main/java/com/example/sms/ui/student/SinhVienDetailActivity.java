package com.example.sms.ui.student;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sms.R;
import com.example.sms.api.AuthApi;
import com.example.sms.api.StudentApi;
import com.example.sms.api.SubjectApi;
import com.example.sms.models.Grade;
import com.example.sms.models.Subject;
import com.example.sms.ui.subject.SubjectAdapter;

import java.util.ArrayList;
import java.util.List;

public class SinhVienDetailActivity extends AppCompatActivity {
    private StudentApi studentApi;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sinh_vien_detail);
        
        getSupportActionBar().setTitle("Chi tiết Sinh viên");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        studentApi = new StudentApi();

        TextView tvName = findViewById(R.id.tv_detail_name);
        TextView tvMssv = findViewById(R.id.tv_detail_mssv);
        LinearLayout adminActions = findViewById(R.id.layout_admin_actions);
        Button btnDelete = findViewById(R.id.btn_delete_student);
        Button btnEdit = findViewById(R.id.btn_edit_student);
        TextView tvLop = findViewById(R.id.tv_detail_lop);

        // Get data from Intent
        String studentId = getIntent().getStringExtra("STUDENT_ID");
        String name = getIntent().getStringExtra("STUDENT_NAME");
        String mssv = getIntent().getStringExtra("STUDENT_MSSV");
        String email = getIntent().getStringExtra("STUDENT_EMAIL");
        String lop = getIntent().getStringExtra("STUDENT_LOP");

        tvName.setText(name);
        tvMssv.setText("MSSV: " + mssv);
        
        if(email != null && !email.isEmpty()){
            TextView tvEmail = findViewById(R.id.tv_detail_email);
            tvEmail.setText("Email: " + email);
        }
        if(lop != null && !lop.isEmpty()){
            tvLop.setText("Lớp: " + lop);
        }

        if (AuthApi.isAdmin) {
            adminActions.setVisibility(View.VISIBLE);
        } else {
            adminActions.setVisibility(View.GONE);
        }

        RecyclerView rvSubjects = findViewById(R.id.rv_student_subjects);
        rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        StudentSubjectAdapter adapter = new StudentSubjectAdapter(studentId, subject -> {
            showGradesDialog(studentId, subject);
        });
        rvSubjects.setAdapter(adapter);

        SubjectApi subjectApi = new SubjectApi();
        subjectApi.getSubjectsForStudent(studentId, new com.example.sms.api.ApiCallback<List<Subject>>() {
            @Override
            public void onSuccess(List<Subject> result) {
                adapter.setSubjects(result);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SinhVienDetailActivity.this, "Lỗi tải danh sách môn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // (Removed long click to remove subject, since StudentSubjectAdapter handles clicks for grades now. 
        // Admin could still remove via another button, but for this task grades take priority. 
        // We'll leave the remove functionality out of the item click for now to keep it simple.)

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Xóa sinh viên")
                .setMessage("Bạn có chắc chắn muốn xóa sinh viên này?")
                .setPositiveButton("Có", (dialog, which) -> {
                    studentApi.deleteStudent(studentId, new com.example.sms.api.ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(SinhVienDetailActivity.this, "Đã xóa sinh viên", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(SinhVienDetailActivity.this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Không", null)
                .show();
        });

        btnEdit.setOnClickListener(v -> {
            showEditDialog(studentId, name, mssv, email, lop);
        });
    }

    private void showGradesDialog(String studentId, Subject subject) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_grades, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        TextView tvTitle = dialogView.findViewById(R.id.tv_grade_title);
        EditText etThanhPhan = dialogView.findViewById(R.id.et_diem_thanh_phan);
        EditText etGiuaKy = dialogView.findViewById(R.id.et_diem_giua_ky);
        EditText etCuoiKy = dialogView.findViewById(R.id.et_diem_cuoi_ky);
        TextView tvGpaMon = dialogView.findViewById(R.id.tv_gpa_mon);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_grade);
        Button btnSave = dialogView.findViewById(R.id.btn_save_grade);

        tvTitle.setText("Điểm môn: " + subject.getTenMonHoc());

        // Load existing grades
        studentApi.getStudentGrades(studentId, subject.getId(), new com.example.sms.api.ApiCallback<Grade>() {
            @Override
            public void onSuccess(Grade grade) {
                etThanhPhan.setText(String.valueOf(grade.getDiemThanhPhan()));
                etGiuaKy.setText(String.valueOf(grade.getDiemGiuaKy()));
                etCuoiKy.setText(String.valueOf(grade.getDiemCuoiKy()));
                tvGpaMon.setText(String.format("GPA Môn: %.1f", grade.getGpaMon()));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SinhVienDetailActivity.this, "Lỗi tải điểm", Toast.LENGTH_SHORT).show();
            }
        });

        if (!AuthApi.isAdmin) {
            etThanhPhan.setEnabled(false);
            etGiuaKy.setEnabled(false);
            etCuoiKy.setEnabled(false);
            btnSave.setVisibility(View.GONE);
        } else {
            android.text.TextWatcher watcher = new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(android.text.Editable s) {
                    try {
                        double tp = etThanhPhan.getText().toString().isEmpty() ? 0 : Double.parseDouble(etThanhPhan.getText().toString());
                        double gk = etGiuaKy.getText().toString().isEmpty() ? 0 : Double.parseDouble(etGiuaKy.getText().toString());
                        double ck = etCuoiKy.getText().toString().isEmpty() ? 0 : Double.parseDouble(etCuoiKy.getText().toString());
                        
                        if(tp < 0 || tp > 10 || gk < 0 || gk > 10 || ck < 0 || ck > 10) {
                            tvGpaMon.setText("Điểm phải từ 0 đến 10");
                            return;
                        }

                        Grade tempGrade = new Grade(tp, gk, ck);
                        tvGpaMon.setText(String.format("GPA Môn: %.1f", tempGrade.getGpaMon()));
                    } catch (NumberFormatException e) {
                        tvGpaMon.setText("Điểm không hợp lệ");
                    }
                }
            };
            
            etThanhPhan.addTextChangedListener(watcher);
            etGiuaKy.addTextChangedListener(watcher);
            etCuoiKy.addTextChangedListener(watcher);

            btnSave.setOnClickListener(v -> {
                try {
                    double tp = etThanhPhan.getText().toString().isEmpty() ? 0 : Double.parseDouble(etThanhPhan.getText().toString());
                    double gk = etGiuaKy.getText().toString().isEmpty() ? 0 : Double.parseDouble(etGiuaKy.getText().toString());
                    double ck = etCuoiKy.getText().toString().isEmpty() ? 0 : Double.parseDouble(etCuoiKy.getText().toString());
                    
                    if(tp < 0 || tp > 10 || gk < 0 || gk > 10 || ck < 0 || ck > 10) {
                        Toast.makeText(SinhVienDetailActivity.this, "Điểm phải từ 0 đến 10", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Grade grade = new Grade(tp, gk, ck);
                    studentApi.updateGrades(studentId, subject.getId(), grade, new com.example.sms.api.ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            studentApi.recalculateStudentGpa(studentId, new com.example.sms.api.ApiCallback<Void>() {
                                @Override
                                public void onSuccess(Void res) {
                                    Toast.makeText(SinhVienDetailActivity.this, "Đã lưu điểm và cập nhật GPA", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(SinhVienDetailActivity.this, "Lưu điểm thành công nhưng lỗi cập nhật GPA", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(SinhVienDetailActivity.this, "Lỗi lưu điểm", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (NumberFormatException e) {
                    Toast.makeText(SinhVienDetailActivity.this, "Điểm không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showEditDialog(String studentId, String currentName, String currentMssv, String currentEmail, String currentLop) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        EditText etTen = dialogView.findViewById(R.id.et_ten);
        EditText etMssv = dialogView.findViewById(R.id.et_mssv);
        EditText etEmail = dialogView.findViewById(R.id.et_email);
        EditText etLop = dialogView.findViewById(R.id.et_lop);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        TextView title = dialogView.findViewById(R.id.tv_grade_title); // Not robust, dialog_add_student has text="Thêm Sinh Viên" hardcoded, let's ignore title for now

        etTen.setText(currentName);
        etMssv.setText(currentMssv);
        etEmail.setText(currentEmail);
        etLop.setText(currentLop);
        
        // Khóa không cho sửa email để tránh lệch tài khoản Auth
        etEmail.setEnabled(false);
        etEmail.setAlpha(0.5f);
        Toast.makeText(this, "Chú ý: Không thể sửa Email đăng nhập. Nếu email sai, vui lòng xóa và thêm lại sinh viên.", Toast.LENGTH_LONG).show();

        btnAdd.setText("Lưu");

        btnAdd.setOnClickListener(v -> {
            String ten = etTen.getText().toString().trim();
            String mssv = etMssv.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String lop = etLop.getText().toString().trim();

            if (ten.isEmpty() || mssv.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và MSSV", Toast.LENGTH_SHORT).show();
                return;
            }

            com.example.sms.models.Student updatedStudent = new com.example.sms.models.Student(studentId, ten, mssv, email, lop);
            studentApi.updateStudent(updatedStudent, new com.example.sms.api.ApiCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(SinhVienDetailActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    // Update UI (simplified, better to recreate or update intent)
                    TextView tvName = findViewById(R.id.tv_detail_name);
                    TextView tvMssv = findViewById(R.id.tv_detail_mssv);
                    TextView tvEmail = findViewById(R.id.tv_detail_email);
                    TextView tvLop = findViewById(R.id.tv_detail_lop);
                    
                    tvName.setText(ten);
                    tvMssv.setText("MSSV: " + mssv);
                    tvEmail.setText("Email: " + email);
                    tvLop.setText("Lớp: " + lop);
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(SinhVienDetailActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
