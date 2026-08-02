package com.beat.play.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beat.play.R;
import com.beat.play.model.Announcement;

import java.util.ArrayList;
import java.util.List;

public class AdminAnnouncementAdapter extends RecyclerView.Adapter<AdminAnnouncementAdapter.VH> {

    public interface OnAction {
        void onEdit(Announcement announcement);

        void onDelete(Announcement announcement);
    }

    private final List<Announcement> data = new ArrayList<>();
    private OnAction listener;

    public void setOnAction(OnAction listener) {
        this.listener = listener;
    }

    public void setData(List<Announcement> announcements) {
        data.clear();
        data.addAll(announcements);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_announcement, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Announcement announcement = data.get(position);
        holder.tvTitle.setText(announcement.title != null ? announcement.title : "");
        holder.tvMessage.setText(announcement.message != null ? announcement.message : "");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(announcement);
            }
        });
        holder.imgEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(announcement);
            }
        });
        holder.imgDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(announcement);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage;
        ImageButton imgEdit, imgDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}
