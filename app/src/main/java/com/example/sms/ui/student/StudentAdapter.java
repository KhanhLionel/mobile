package com.example.sms.ui.student;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sms.R;
import com.example.sms.models.Student;
import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private List<Student> studentList = new ArrayList<>();
    private List<Student> studentListFull = new ArrayList<>();

    public interface OnItemLongClickListener {
        void onItemLongClick(Student student);
    }
    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setStudents(List<Student> students) {
        this.studentList = students;
        this.studentListFull = new ArrayList<>(students);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        studentList.clear();
        if (query.isEmpty()) {
            studentList.addAll(studentListFull);
        } else {
            for (Student student : studentListFull) {
                if (student.getTen().toLowerCase().contains(query.toLowerCase()) || 
                    student.getMssv().toLowerCase().contains(query.toLowerCase())) {
                    studentList.add(student);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sinh_vien, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.tvTen.setText(student.getTen());
        holder.tvMssv.setText("MSSV: " + student.getMssv());
        holder.tvGpa.setText("GPA Tổng: " + student.getGpaTong());

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, SinhVienDetailActivity.class);
            intent.putExtra("STUDENT_ID", student.getId());
            intent.putExtra("STUDENT_NAME", student.getTen());
            intent.putExtra("STUDENT_MSSV", student.getMssv());
            intent.putExtra("STUDENT_EMAIL", student.getEmail());
            intent.putExtra("STUDENT_LOP", student.getTenLop());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(student);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvMssv, tvGpa;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTen = itemView.findViewById(R.id.tv_ten_sinh_vien);
            tvMssv = itemView.findViewById(R.id.tv_mssv);
            tvGpa = itemView.findViewById(R.id.tv_gpa);
        }
    }
}
