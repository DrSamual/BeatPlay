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
import com.beat.play.model.Banner;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.VH> {

    public interface OnItemClick {
        void onClick(Banner banner);
    }

    private final List<Banner> data = new ArrayList<>();
    private OnItemClick listener;

    public void setOnItemClickListener(OnItemClick listener) {
        this.listener = listener;
    }

    public List<Banner> getData() {
        return data;
    }

    public void setData(List<Banner> banners) {
        data.clear();
        data.addAll(banners);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.isEmpty() ? 0 : Integer.MAX_VALUE;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Banner banner = data.get(position % data.size());
        holder.tvTitle.setText(banner.title != null ? banner.title : "");
        Glide.with(holder.itemView.getContext())
                .load(banner.image)
                .placeholder(R.drawable.ic_tv)
                .error(R.drawable.ic_tv)
                .into(holder.imgBanner);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(banner);
            }
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView imgBanner;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBannerTitle);
            imgBanner = itemView.findViewById(R.id.imgBanner);
        }
    }
}
