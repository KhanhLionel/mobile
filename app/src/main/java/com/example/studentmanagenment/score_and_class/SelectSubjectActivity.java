package com.example.studentmanagenment.score_and_class;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentmanagenment.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SelectSubjectActivity extends AppCompatActivity {

    private Spinner spinnerSemester;
    private RecyclerView rvSelectSubjects;
    private SubjectAdapter adapter;
    private List<SubjectModel> listSubjects;
    private DatabaseReference subjectsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_subject);

        spinnerSemester = findViewById(R.id.spinnerSemester);
        rvSelectSubjects = findViewById(R.id.rvSelectSubjects);

        listSubjects = new ArrayList<>();
        rvSelectSubjects.setLayoutManager(new LinearLayoutManager(this));

        // TẬN DỤNG LẠI SubjectAdapter CŨ CỦA BẠN
        adapter = new SubjectAdapter(this, listSubjects);
        rvSelectSubjects.setAdapter(adapter);

        subjectsRef = FirebaseDatabase.getInstance().getReference("subjects");

        // 1. Cài đặt dữ liệu cho Spinner Học kỳ
        setupSemesterSpinner();
    }

    private void setupSemesterSpinner() {
        ArrayList<String> semesters = new ArrayList<>();
        semesters.add("Học kỳ 1 - Năm học 2025-2026");
        semesters.add("Học kỳ 2 - Năm học 2025-2026");
        semesters.add("Học kỳ Hè - Năm học 2025-2026");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesters);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(spinnerAdapter);

        // Lắng nghe sự kiện chọn học kỳ để lọc môn học tương ứng
        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSemester = semesters.get(position);
                loadSubjectsBySemester(selectedSemester);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadSubjectsBySemester(String semester) {
        // Lấy toàn bộ môn học về (Nếu Firebase của bạn có chia node theo học kỳ thì trỏ thẳng vào,
        // còn không thì lấy hết về rồi lọc theo học kỳ)
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listSubjects.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    SubjectModel subject = data.getValue(SubjectModel.class);
                    if (subject != null) {
                        listSubjects.add(subject);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectSubjectActivity.this, "Lỗi Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}