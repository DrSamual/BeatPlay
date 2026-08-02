package com.beat.play.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.beat.play.R;
import com.beat.play.model.Channel;

import java.util.ArrayList;
import java.util.List;

public class HomeChannelAdapter extends RecyclerView.Adapter<HomeChannelAdapter.VH> {

    public interface OnItemClick {
        void onClick(Channel channel);
    }

    private final List<Channel> data = new ArrayList<>();
    private OnItemClick listener;

    public void setOnItemClickListener(OnItemClick listener) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel_h, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Channel channel = data.get(position);
        holder.tvName.setText(channel.name != null ? channel.name : "");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(channel);
            }
        });
        if (channel.logo != null && !channel.logo.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(channel.logo)
                    .placeholder(R.drawable.ic_tv)
                    .error(R.drawable.ic_tv)
                    .into(holder.imgLogo);
        } else {
            holder.imgLogo.setImageResource(R.drawable.ic_tv);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imgLogo;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvNameH);
            imgLogo = itemView.findViewById(R.id.imgLogoH);
        }
    }
}
