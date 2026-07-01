package com.example.sms.ui.subject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sms.R;
import com.example.sms.api.AuthApi;
import com.example.sms.api.SubjectApi;
import com.example.sms.api.StudentApi;
import com.example.sms.models.Subject;
import com.example.sms.models.Student;
import com.example.sms.ui.student.StudentAdapter;

import java.util.ArrayList;
import java.util.List;

public class MonHocDetailActivity extends AppCompatActivity {
    private SubjectApi subjectApi;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mon_hoc_detail);
        
        getSupportActionBar().setTitle("Chi tiết Môn học");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        subjectApi = new SubjectApi();

        TextView tvTenMon = findViewById(R.id.tv_detail_mon);
        TextView tvGiangVien = findViewById(R.id.tv_detail_giang_vien);
        TextView tvCaHoc = findViewById(R.id.tv_detail_ca_hoc);
        LinearLayout adminActions = findViewById(R.id.layout_admin_actions_subject);
        Button btnDelete = findViewById(R.id.btn_delete_subject);
        Button btnEdit = findViewById(R.id.btn_edit_subject);
        ImageButton btnAddStudent = findViewById(R.id.btn_add_student_to_subject);

        String subjectId = getIntent().getStringExtra("SUBJECT_ID");
        String name = getIntent().getStringExtra("SUBJECT_NAME");
        String teacher = getIntent().getStringExtra("SUBJECT_TEACHER");
        String time = getIntent().getStringExtra("SUBJECT_TIME");

        tvTenMon.setText(name);
        tvGiangVien.setText("Giảng viên: " + teacher);
        tvCaHoc.setText("Ca học: " + time);

        if (AuthApi.isAdmin) {
            adminActions.setVisibility(View.VISIBLE);
            btnAddStudent.setVisibility(View.VISIBLE);
        } else {
            adminActions.setVisibility(View.GONE);
            btnAddStudent.setVisibility(View.GONE);
        }

        RecyclerView rvStudents = findViewById(R.id.rv_subject_students);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        StudentAdapter adapter = new StudentAdapter();
        rvStudents.setAdapter(adapter);

        List<Student> enrolledStudents = new ArrayList<>();
        StudentApi studentApi = new StudentApi();

        // Load enrolled students
        subjectApi.getStudentsForSubject(subjectId, studentApi, new com.example.sms.api.ApiCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> result) {
                enrolledStudents.clear();
                enrolledStudents.addAll(result);
                adapter.setStudents(enrolledStudents);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MonHocDetailActivity.this, "Lỗi tải danh sách: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        if (AuthApi.isAdmin) {
            adapter.setOnItemLongClickListener(student -> {
                new AlertDialog.Builder(MonHocDetailActivity.this)
                    .setTitle("Xóa khỏi lớp học phần")
                    .setMessage("Xóa sinh viên " + student.getTen() + " khỏi môn này?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        subjectApi.removeStudentFromSubject(student.getId(), subjectId, new com.example.sms.api.ApiCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Toast.makeText(MonHocDetailActivity.this, "Đã xóa khỏi lớp", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(MonHocDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Không", null)
                    .show();
            });
        }

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Xóa môn học")
                .setMessage("Bạn có chắc chắn muốn xóa môn học này?")
                .setPositiveButton("Có", (dialog, which) -> {
                    subjectApi.deleteSubject(subjectId, new com.example.sms.api.ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(MonHocDetailActivity.this, "Đã xóa môn học", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(MonHocDetailActivity.this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Không", null)
                .show();
        });

        btnEdit.setOnClickListener(v -> {
            showEditDialog(subjectId, name, teacher, time);
        });

        btnAddStudent.setOnClickListener(v -> {
            studentApi.getAllStudents(new com.example.sms.api.ApiCallback<java.util.List<Student>>() {
                @Override
                public void onSuccess(java.util.List<Student> students) {
                    if (students.isEmpty()) {
                        Toast.makeText(MonHocDetailActivity.this, "Không có sinh viên nào!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Filter out already enrolled students
                    List<Student> availableStudents = new ArrayList<>();
                    for (Student s : students) {
                        boolean isEnrolled = false;
                        for (Student e : enrolledStudents) {
                            if (e.getId().equals(s.getId())) {
                                isEnrolled = true;
                                break;
                            }
                        }
                        if (!isEnrolled) {
                            availableStudents.add(s);
                        }
                    }

                    if (availableStudents.isEmpty()) {
                        Toast.makeText(MonHocDetailActivity.this, "Tất cả sinh viên đã học môn này!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String[] studentNames = new String[availableStudents.size()];
                    for (int i = 0; i < availableStudents.size(); i++) {
                        studentNames[i] = availableStudents.get(i).getTen() + " (" + availableStudents.get(i).getMssv() + ")";
                    }
                    
                    int[] checkedItem = {-1};
                    new AlertDialog.Builder(MonHocDetailActivity.this)
                        .setTitle("Chọn Sinh Viên")
                        .setSingleChoiceItems(studentNames, -1, (dialog, which) -> {
                            checkedItem[0] = which;
                        })
                        .setPositiveButton("Thêm", (dialog, which) -> {
                            if (checkedItem[0] != -1) {
                                Student selectedStudent = availableStudents.get(checkedItem[0]);
                                Subject currentSubject = new Subject(subjectId, name, teacher, time);
                                subjectApi.addStudentToSubject(selectedStudent.getId(), currentSubject, new com.example.sms.api.ApiCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        Toast.makeText(MonHocDetailActivity.this, "Thêm sinh viên thành công!", Toast.LENGTH_SHORT).show();
                                    }
                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(MonHocDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(MonHocDetailActivity.this, "Lỗi tải danh sách: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showEditDialog(String subjectId, String currentName, String currentTeacher, String currentTime) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_subject, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        EditText etTenMon = dialogView.findViewById(R.id.et_ten_mon);
        EditText etGiangVien = dialogView.findViewById(R.id.et_giang_vien);
        EditText etCaHoc = dialogView.findViewById(R.id.et_ca_hoc);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        etTenMon.setText(currentName);
        etGiangVien.setText(currentTeacher);
        etCaHoc.setText(currentTime);
        btnAdd.setText("Lưu");

        btnAdd.setOnClickListener(v -> {
            String tenMon = etTenMon.getText().toString().trim();
            String giangVien = etGiangVien.getText().toString().trim();
            String caHoc = etCaHoc.getText().toString().trim();

            if (tenMon.isEmpty() || caHoc.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên môn và ca học", Toast.LENGTH_SHORT).show();
                return;
            }

            Subject updatedSubject = new Subject(subjectId, tenMon, giangVien, caHoc);
            subjectApi.updateSubject(updatedSubject, new com.example.sms.api.ApiCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(MonHocDetailActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    // Cập nhật lại UI
                    TextView tvTenMon = findViewById(R.id.tv_detail_mon);
                    TextView tvGiangVien = findViewById(R.id.tv_detail_giang_vien);
                    TextView tvCaHoc = findViewById(R.id.tv_detail_ca_hoc);
                    
                    tvTenMon.setText(tenMon);
                    tvGiangVien.setText("Giảng viên: " + giangVien);
                    tvCaHoc.setText("Ca học: " + caHoc);
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(MonHocDetailActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
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
