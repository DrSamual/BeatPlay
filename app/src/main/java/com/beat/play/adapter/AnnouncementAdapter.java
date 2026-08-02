package com.beat.play.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beat.play.R;
import com.beat.play.model.Announcement;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.VH> {

    private final List<Announcement> data = new ArrayList<>();

    public void setData(List<Announcement> announcements) {
        data.clear();
        data.addAll(announcements);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Announcement announcement = data.get(position);
        holder.tvTitle.setText(announcement.title != null ? announcement.title : "");
        holder.tvMessage.setText(announcement.message != null ? announcement.message : "");
        holder.tvTime.setText(Announcement.formatTime(announcement.timestamp));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
