package com.example.studentmanagenment.score_and_class;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentmanagenment.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class StudentScoreActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private List<StudentScore> studentList;
    private TextView tvEmptyMessage, tvStudentNameTitle, tvStudentIdTitle, tvTotalGpa;
    private FloatingActionButton fabAddStudent;
    private ImageButton btnConfigPercent;

    // KHÔNG gán cứng nữa, để biến rỗng để Intent truyền vào tự nhận
    private String subjectId;
    private String subjectName;
    private int pctQT = 20, pctGK = 30, pctCK = 50;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_score_list);

        // 1. LẤY DỮ LIỆU ĐỘNG TỪ INTENT ĐƯỢC TRUYỀN SANG
        subjectId = getIntent().getStringExtra("KEY_SUBJECT_ID");
        subjectName = getIntent().getStringExtra("KEY_SUBJECT_NAME");

        // Phòng trường hợp Intent bị lỗi không truyền qua được thì gán mặc định để chống crash app
        if (subjectId == null) subjectId = "MH01";
        if (subjectName == null) subjectName = "Môn học phần";

        // Ánh xạ các thành phần giao diện
        recyclerView = findViewById(R.id.rvSubjectScores);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        tvStudentNameTitle = findViewById(R.id.tvStudentNameTitle);
        tvStudentIdTitle = findViewById(R.id.tvStudentIdTitle);
        tvTotalGpa = findViewById(R.id.tvTotalGpa);
        fabAddStudent = findViewById(R.id.fabAddStudent);
        btnConfigPercent = findViewById(R.id.btnConfigPercent);

        // 2. HIỂN THỊ ĐÚNG TÊN VÀ MÃ MÔN LÊN HEADER
        tvStudentNameTitle.setText(subjectName);
        tvStudentIdTitle.setText("Mã môn học: " + subjectId);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentList = new ArrayList<>();
        adapter = new StudentAdapter(studentList, subjectId, pctQT, pctGK, pctCK);
        recyclerView.setAdapter(adapter);

        // Kết nối chuẩn đến đúng Node môn học đã chọn
        dbRef = FirebaseDatabase.getInstance().getReference("subject_members").child(subjectId);

        loadDataFromFirebase();

        // LOGIC NÚT CÀI ĐẶT % ĐIỂM
        btnConfigPercent.setOnClickListener(v -> {
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(60, 40, 60, 20);

            EditText etQT = new EditText(this); etQT.setHint("Phần trăm Quá trình (%)"); etQT.setText(String.valueOf(pctQT));
            EditText etGK = new EditText(this); etGK.setHint("Phần trăm Giữa kỳ (%)"); etGK.setText(String.valueOf(pctGK));
            EditText etCK = new EditText(this); etCK.setHint("Phần trăm Cuối kỳ (%)"); etCK.setText(String.valueOf(pctCK));

            etQT.setInputType(InputType.TYPE_CLASS_NUMBER);
            etGK.setInputType(InputType.TYPE_CLASS_NUMBER);
            etCK.setInputType(InputType.TYPE_CLASS_NUMBER);

            layout.addView(etQT); layout.addView(etGK); layout.addView(etCK);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Cấu hình tỷ lệ phần trăm")
                    .setView(layout)
                    .setCancelable(false)
                    .setPositiveButton("Lưu cấu hình", null)
                    .setNegativeButton("Hủy", null)
                    .create();

            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                try {
                    int qt = Integer.parseInt(etQT.getText().toString().trim());
                    int gk = Integer.parseInt(etGK.getText().toString().trim());
                    int ck = Integer.parseInt(etCK.getText().toString().trim());

                    if (qt + gk + ck != 100) {
                        Toast.makeText(this, "Lỗi: Tổng 3 cột điểm phải bằng 100%!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pctQT = qt;
                    pctGK = gk;
                    pctCK = ck;

                    adapter.updatePercentages(qt, gk, ck);
                    recalculateClassAverage();

                    Toast.makeText(this, "Đã cập nhật cấu hình tỷ lệ điểm mới!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                } catch (Exception e) {
                    Toast.makeText(this, "Vui lòng nhập đúng định dạng số nguyên!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // XỬ LÝ SỰ KIỆN: THÊM SINH VIÊN
        fabAddStudent.setOnClickListener(v -> {
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(60, 40, 60, 20);

            EditText etId = new EditText(this); etId.setHint("Nhập MSSV (Bắt buộc)");
            EditText etName = new EditText(this); etName.setHint("Nhập Họ và tên (Bắt buộc)");
            layout.addView(etId); layout.addView(etName);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Thêm sinh viên mới")
                    .setView(layout)
                    .setCancelable(false)
                    .setPositiveButton("Thêm", null)
                    .setNegativeButton("Hủy", null)
                    .create();

            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String id = etId.getText().toString().trim();
                String name = etName.getText().toString().trim();

                if (id.isEmpty() || name.isEmpty()) {
                    Toast.makeText(this, "Lỗi: Không được bỏ trống Tên hoặc MSSV!", Toast.LENGTH_SHORT).show();
                    return;
                }

                StudentScore newStudent = new StudentScore(id, name, 0.0, 0.0, 0.0);
                dbRef.child(id).setValue(newStudent).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã thêm sinh viên thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            });
        });
    }

    private void loadDataFromFirebase() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    StudentScore student = data.getValue(StudentScore.class);
                    if (student != null) {
                        if (student.getStudentId() != null && student.getStudentName() != null) {
                            studentList.add(student);
                        }
                    }
                }
                recalculateClassAverage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentScoreActivity.this, "Lỗi kết nối Firebase!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recalculateClassAverage() {
        if (studentList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyMessage.setVisibility(View.VISIBLE);
            tvTotalGpa.setText("0.00");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyMessage.setVisibility(View.GONE);

            double totalSum = 0;
            for (StudentScore student : studentList) {
                totalSum += student.getCalculatedAvg(pctQT, pctGK, pctCK);
            }
            double finalClassAvg = totalSum / studentList.size();
            tvTotalGpa.setText(String.format("%.2f", finalClassAvg));
        }
        adapter.notifyDataSetChanged();
    }
}