package com.example.sms.ui.subject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sms.R;
import com.example.sms.models.Subject;
import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {
    private List<Subject> subjectList = new ArrayList<>();
    private List<Subject> subjectListFull = new ArrayList<>();

    public interface OnItemLongClickListener {
        void onItemLongClick(Subject subject);
    }
    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjectList = subjects;
        this.subjectListFull = new ArrayList<>(subjects);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        subjectList.clear();
        if (query.isEmpty()) {
            subjectList.addAll(subjectListFull);
        } else {
            for (Subject subject : subjectListFull) {
                if (subject.getTenMonHoc().toLowerCase().contains(query.toLowerCase()) || 
                    subject.getCaHoc().toLowerCase().contains(query.toLowerCase())) {
                    subjectList.add(subject);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mon_hoc, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.tvTenMon.setText(subject.getTenMonHoc());
        holder.tvGiangVien.setText("Giảng viên: " + subject.getGiangVien());
        holder.tvCaHoc.setText("Ca học: " + subject.getCaHoc());

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, MonHocDetailActivity.class);
            intent.putExtra("SUBJECT_ID", subject.getId());
            intent.putExtra("SUBJECT_NAME", subject.getTenMonHoc());
            intent.putExtra("SUBJECT_TEACHER", subject.getGiangVien());
            intent.putExtra("SUBJECT_TIME", subject.getCaHoc());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(subject);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenMon, tvGiangVien, tvCaHoc;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenMon = itemView.findViewById(R.id.tv_ten_mon);
            tvGiangVien = itemView.findViewById(R.id.tv_giang_vien);
            tvCaHoc = itemView.findViewById(R.id.tv_ca_hoc);
        }
    }
}
