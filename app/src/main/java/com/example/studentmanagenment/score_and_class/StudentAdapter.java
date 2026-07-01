package com.example.studentmanagenment.score_and_class;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentmanagenment.R;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<StudentScore> studentList;
    private String subjectId;
    private int pctQT, pctGK, pctCK;

    public StudentAdapter(List<StudentScore> studentList, String subjectId, int pctQT, int pctGK, int pctCK) {
        this.studentList = studentList;
        this.subjectId = subjectId;
        this.pctQT = pctQT;
        this.pctGK = pctGK;
        this.pctCK = pctCK;
    }

    public void updatePercentages(int qt, int gk, int ck) {
        this.pctQT = qt;
        this.pctGK = gk;
        this.pctCK = ck;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        if (studentList == null || position >= studentList.size()) return;

        StudentScore student = studentList.get(position);
        if (student == null) return;

        Context context = holder.itemView.getContext();

        holder.tvStudentName.setText(student.getStudentName() != null ? student.getStudentName() : "Chưa có tên");
        holder.tvStudentId.setText("MSSV: " + (student.getStudentId() != null ? student.getStudentId() : "---"));

        // Gọi hàm tính ĐTB động vừa viết trong Model
        double avg = student.getCalculatedAvg(pctQT, pctGK, pctCK);
        holder.tvScoreDTB.setText("ĐTB: " + avg);

        // Đổi màu chữ ĐTB dựa theo học lực
        if (avg >= 8.0) holder.tvScoreDTB.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
        else if (avg >= 5.0) holder.tvScoreDTB.setTextColor(android.graphics.Color.parseColor("#F57C00"));
        else holder.tvScoreDTB.setTextColor(android.graphics.Color.parseColor("#D32F2F"));

        // SỰ KIỆN: Sửa tên sinh viên (Hồ Sơ)
        holder.btnProfile.setOnClickListener(v -> {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 30, 50, 10);

            EditText etName = new EditText(context);
            etName.setHint("Họ và tên sinh viên");
            etName.setInputType(InputType.TYPE_CLASS_TEXT);
            etName.setText(student.getStudentName());
            layout.addView(etName);

            new AlertDialog.Builder(context)
                    .setTitle("Sửa thông tin sinh viên")
                    .setView(layout)
                    .setPositiveButton("Cập nhật", (dialog, which) -> {
                        String newName = etName.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            student.setStudentName(newName);
                            FirebaseDatabase.getInstance().getReference("subject_members")
                                    .child(subjectId).child(student.getStudentId()).setValue(student)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã sửa tên thành công!", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .setNegativeButton("Hủy", null).show();
        });

        // SỰ KIỆN: Nhập điểm tại chỗ
        holder.btnEnterScore.setOnClickListener(v -> {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 30, 50, 10);

            EditText etQT = new EditText(context); etQT.setHint("Điểm Quá trình"); etQT.setText(String.valueOf(student.getProcessScore()));
            EditText etGK = new EditText(context); etGK.setHint("Điểm Giữa kỳ"); etGK.setText(String.valueOf(student.getMidtermScore()));
            EditText etCK = new EditText(context); etCK.setHint("Điểm Cuối kỳ"); etCK.setText(String.valueOf(student.getFinalScore()));
            etQT.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etGK.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etCK.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(etQT); layout.addView(etGK); layout.addView(etCK);

            new AlertDialog.Builder(context)
                    .setTitle("Vào điểm cho: " + student.getStudentName())
                    .setView(layout)
                    .setPositiveButton("Lưu", (dialog, which) -> {
                        try {
                            double qt = Double.parseDouble(etQT.getText().toString().trim());
                            double gk = Double.parseDouble(etGK.getText().toString().trim());
                            double ck = Double.parseDouble(etCK.getText().toString().trim());

                            student.setProcessScore(qt);
                            student.setMidtermScore(gk);
                            student.setFinalScore(ck);

                            FirebaseDatabase.getInstance().getReference("subject_members")
                                    .child(subjectId).child(student.getStudentId()).setValue(student)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Đã cập nhật bảng điểm!", Toast.LENGTH_SHORT).show();
                                        notifyDataSetChanged();
                                    });
                        } catch (Exception e) {
                            Toast.makeText(context, "Số điểm nhập vào không hợp lệ!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null).show();
        });

        // SỰ KIỆN: Xóa sinh viên
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn muốn xóa " + student.getStudentName() + " khỏi lớp?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference("subject_members")
                                .child(subjectId).child(student.getStudentId()).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã xóa sinh viên!", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Hủy", null).show();
        });
    }

    @Override
    public int getItemCount() { return studentList != null ? studentList.size() : 0; }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentId, tvScoreDTB;
        Button btnProfile, btnEnterScore;
        ImageView btnDelete;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvScoreDTB = itemView.findViewById(R.id.tvScoreDTB);
            btnProfile = itemView.findViewById(R.id.btnProfile);
            btnEnterScore = itemView.findViewById(R.id.btnEnterScore);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}