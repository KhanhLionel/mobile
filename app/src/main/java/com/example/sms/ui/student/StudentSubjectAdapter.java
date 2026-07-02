package com.example.sms.ui.student;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sms.R;
import com.example.sms.api.ApiCallback;
import com.example.sms.api.StudentApi;
import com.example.sms.models.Grade;
import com.example.sms.models.Subject;

import java.util.ArrayList;
import java.util.List;

public class StudentSubjectAdapter extends RecyclerView.Adapter<StudentSubjectAdapter.ViewHolder> {
    private List<Subject> subjectList = new ArrayList<>();
    private String studentId;
    private OnGradeClickListener listener;
    private StudentApi studentApi = new StudentApi();

    public interface OnGradeClickListener {
        void onGradeClick(Subject subject);
    }

    public StudentSubjectAdapter(String studentId, OnGradeClickListener listener) {
        this.studentId = studentId;
        this.listener = listener;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjectList = subjects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mon_hoc_in_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.tvTenMon.setText(subject.getTenMonHoc());
        holder.tvCaHoc.setText("Ca học: " + subject.getCaHoc());

        // Load grade for this subject
        studentApi.getStudentGrades(studentId, subject.getId(), new ApiCallback<Grade>() {
            @Override
            public void onSuccess(Grade grade) {
                holder.tvTenMon.setText(subject.getTenMonHoc() + String.format(" (GPA: %.1f)", grade.getGpaMon()));
            }

            @Override
            public void onFailure(Exception e) {
                // Ignore error
            }
        });

        holder.btnGrades.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGradeClick(subject);
            }
        });
        
        // Also allow clicking the whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGradeClick(subject);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenMon;
        TextView tvCaHoc;
        ImageButton btnGrades;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenMon = itemView.findViewById(R.id.tv_ten_mon);
            tvCaHoc = itemView.findViewById(R.id.tv_ca_hoc);
            btnGrades = itemView.findViewById(R.id.btn_grades);
        }
    }
}
