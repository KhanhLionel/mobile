package com.example.studentmanagenment.score_and_class;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
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

public class SubjectListActivity extends AppCompatActivity {

    private TextView tvTotalSubjects;
    private RecyclerView rvSubjects;
    private SubjectAdapter adapter;
    private List<SubjectModel> listSubjects; // 🛠 Đã đổi từ SubjectScore sang SubjectModel
    private FloatingActionButton fabAddSubject;

    private DatabaseReference subjectsDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_list);

        tvTotalSubjects = findViewById(R.id.tvTotalSubjects);
        rvSubjects = findViewById(R.id.rvSubjects);
        fabAddSubject = findViewById(R.id.fabAddSubject);

        listSubjects = new ArrayList<>();
        // 🛠 Khởi tạo Adapter với List<SubjectModel> và Context đúng thứ tự tham số của bạn
        adapter = new SubjectAdapter(this, listSubjects);
        rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        rvSubjects.setAdapter(adapter);

        // Trỏ Firebase kết nối thẳng tới node danh mục các môn học
        subjectsDatabaseRef = FirebaseDatabase.getInstance().getReference("subjects");

        // Lắng nghe dữ liệu danh mục môn từ Firebase đổ về RecyclerView
        subjectsDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listSubjects.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    // 🛠 Ép kiểu về đúng SubjectModel
                    SubjectModel subject = data.getValue(SubjectModel.class);
                    if (subject != null) {
                        listSubjects.add(subject);
                    }
                }
                adapter.notifyDataSetChanged();
                tvTotalSubjects.setText("Tổng số: " + listSubjects.size() + " môn học");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SubjectListActivity.this, "Lỗi kết nối Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Click nút dấu cộng [+] để bung Dialog mở thêm lớp môn học mới
        fabAddSubject.setOnClickListener(v -> showAddSubjectDialog());
    }

    private void showAddSubjectDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_input_score); // Tận dụng lại form mẫu dialog nhập liệu của bạn
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDialogSubjectName);
        EditText edtId = dialog.findViewById(R.id.edtProcessScore);
        EditText edtName = dialog.findViewById(R.id.edtMidtermScore);
        EditText edtCredits = dialog.findViewById(R.id.edtFinalScore);
        Button btnCancel = dialog.findViewById(R.id.btnCancelDialog);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmitScore);

        tvTitle.setText("Thêm môn học học phần mới");
        edtId.setHint("Nhập Mã môn học (Ví dụ: MH03)");
        edtId.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        edtId.setText("");

        edtName.setHint("Nhập Tên môn học (Ví dụ: Lập trình Java)");
        edtName.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        edtName.setText("");

        edtCredits.setHint("Nhập Số tín chỉ (Ví dụ: 3)");
        edtCredits.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        edtCredits.setText("");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String idStr = edtId.getText().toString().trim();
            String nameStr = edtName.getText().toString().trim();
            String creditStr = edtCredits.getText().toString().trim();

            if (idStr.isEmpty() || nameStr.isEmpty() || creditStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin môn học!", Toast.LENGTH_SHORT).show();
                return;
            }

            int credits = Integer.parseInt(creditStr);

            // 🛠 Khởi tạo môn học mới thuộc lớp SubjectModel
            // Đặt cấu hình mặc định ban đầu là 20% - 30% - 50% để tránh lỗi trống dữ liệu phần trăm
            SubjectModel newSubject = new SubjectModel(idStr, nameStr, credits, 20, 30, 50);

            // Đẩy đồng bộ dữ liệu lên node danh mục môn trên Firebase
            subjectsDatabaseRef.child(idStr).setValue(newSubject)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SubjectListActivity.this, "Đã thêm môn học mới lên hệ thống!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(SubjectListActivity.this, "Lỗi thêm môn: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }
}