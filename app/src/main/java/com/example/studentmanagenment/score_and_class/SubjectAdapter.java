package com.example.studentmanagenment.score_and_class;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagenment.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private Context context;
    private List<SubjectModel> listSubjects; // Đã đổi sang SubjectModel

    public SubjectAdapter(Context context, List<SubjectModel> listSubjects) {
        this.context = context;
        this.listSubjects = listSubjects;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        SubjectModel currentSubject = listSubjects.get(position); // Đã đổi sang SubjectModel

        // Đổ dữ liệu lên giao diện
        holder.itemTvSubjectName.setText(currentSubject.getSubjectName());
        holder.itemTvSubjectId.setText("Mã môn: " + currentSubject.getSubjectId());
        holder.itemTvCredits.setText(currentSubject.getCredits() + " TC");

        // Sự kiện xem danh sách sinh viên
        holder.btnViewStudents.setOnClickListener(v -> {
            Intent intent = new Intent(context, StudentScoreActivity.class);
            // Truyền mã môn học đang bấm sang màn hình quản lý điểm
            intent.putExtra("SUBJECT_ID", currentSubject.getSubjectId());
            context.startActivity(intent);
        });

        // Sự kiện xóa môn học
        holder.btnDeleteSubject.setOnClickListener(v -> {
            // Viết logic xóa môn học nếu cần
        });

        // Bắt sự kiện click vào nút 3 chấm đứng để cấu hình tỷ lệ điểm
        holder.btnSubjectMoreOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.getMenu().add(1, 1, 1, "Cấu hình tỷ lệ điểm (%)");

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    hienThiDialogSuaPhanTram(currentSubject.getSubjectId());
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return listSubjects != null ? listSubjects.size() : 0;
    }

    // Hàm hiển thị hộp thoại nhập phần trăm điểm
    private void hienThiDialogSuaPhanTram(String subjectId) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_config_percent, null);
        builder.setView(dialogView);

        EditText edtProcess = dialogView.findViewById(R.id.edtPercentProcess);
        EditText edtMidterm = dialogView.findViewById(R.id.edtPercentMidterm);
        EditText edtFinal = dialogView.findViewById(R.id.edtPercentFinal);

        // Đọc dữ liệu cũ có sẵn từ Firebase Realtime Database
        DatabaseReference subjectRef = FirebaseDatabase.getInstance().getReference("subjects").child(subjectId);
        subjectRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                Long pProc = dataSnapshot.child("percentProcess").getValue(Long.class);
                Long pMid = dataSnapshot.child("percentMidterm").getValue(Long.class);
                Long pFin = dataSnapshot.child("percentFinal").getValue(Long.class);

                if (pProc != null) edtProcess.setText(String.valueOf(pProc));
                if (pMid != null) edtMidterm.setText(String.valueOf(pMid));
                if (pFin != null) edtFinal.setText(String.valueOf(pFin));
            }
        });

        builder.setPositiveButton("Lưu", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String strProcess = edtProcess.getText().toString().trim();
            String strMidterm = edtMidterm.getText().toString().trim();
            String strFinal = edtFinal.getText().toString().trim();

            if (strProcess.isEmpty() || strMidterm.isEmpty() || strFinal.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ cả 3 cột điểm!", Toast.LENGTH_SHORT).show();
                return;
            }

            int pProcess = Integer.parseInt(strProcess);
            int pMidterm = Integer.parseInt(strMidterm);
            int pFinal = Integer.parseInt(strFinal);

            if (pProcess + pMidterm + pFinal != 100) {
                Toast.makeText(context, "Lỗi: Tổng 3 cột phải đúng bằng 100%!", Toast.LENGTH_LONG).show();
                return;
            }

            // Lưu lên Firebase
            subjectRef.child("percentProcess").setValue(pProcess);
            subjectRef.child("percentMidterm").setValue(pMidterm);
            subjectRef.child("percentFinal").setValue(pFinal)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Cập nhật tỷ lệ thành công!", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi lưu lên Firebase!", Toast.LENGTH_SHORT).show());
        });
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView itemTvSubjectName, itemTvSubjectId, itemTvCredits;
        Button btnViewStudents;
        ImageButton btnDeleteSubject;
        ImageButton btnSubjectMoreOptions;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTvSubjectName = itemView.findViewById(R.id.itemTvSubjectName);
            itemTvSubjectId = itemView.findViewById(R.id.itemTvSubjectId);
            itemTvCredits = itemView.findViewById(R.id.itemTvCredits);
            btnViewStudents = itemView.findViewById(R.id.btnViewStudents);
            btnDeleteSubject = itemView.findViewById(R.id.btnDeleteSubject);
            btnSubjectMoreOptions = itemView.findViewById(R.id.btnSubjectMoreOptions);
        }
    }
}