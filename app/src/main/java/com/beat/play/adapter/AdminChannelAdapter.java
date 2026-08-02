package com.beat.play.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beat.play.R;
import com.beat.play.model.Channel;

import java.util.ArrayList;
import java.util.List;

public class AdminChannelAdapter extends RecyclerView.Adapter<AdminChannelAdapter.VH> {

    public interface OnAction {
        void onEdit(Channel channel);

        void onDelete(Channel channel);
    }

    private final List<Channel> data = new ArrayList<>();
    private OnAction listener;

    public void setOnAction(OnAction listener) {
        this.listener = listener;
    }

    public void setData(List<Channel> channels) {
        data.clear();
        data.addAll(channels);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_channel, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Channel channel = data.get(position);
        holder.tvName.setText(channel.name != null ? channel.name : "");
        holder.tvCategory.setText(channel.category != null ? channel.category : "লাইভ");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(channel);
            }
        });
        holder.imgEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(channel);
            }
        });
        holder.imgDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(channel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory;
        ImageButton imgEdit, imgDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}
