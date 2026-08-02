package com.beat.play.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.beat.play.R;
import com.beat.play.model.Banner;

import java.util.ArrayList;
import java.util.List;

public class AdminBannerAdapter extends RecyclerView.Adapter<AdminBannerAdapter.VH> {

    public interface OnAction {
        void onEdit(Banner banner);

        void onDelete(Banner banner);
    }

    private final List<Banner> data = new ArrayList<>();
    private OnAction listener;

    public void setOnAction(OnAction listener) {
        this.listener = listener;
    }

    public void setData(List<Banner> banners) {
        data.clear();
        data.addAll(banners);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_banner, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Banner banner = data.get(position);
        holder.tvTitle.setText(banner.title != null ? banner.title : "");
        holder.tvTarget.setText(banner.targetTitle != null ? "→ " + banner.targetTitle : "");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(banner);
            }
        });
        holder.imgEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(banner);
            }
        });
        holder.imgDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(banner);
            }
        });
        if (banner.image != null && !banner.image.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(banner.image)
                    .placeholder(R.drawable.ic_tv)
                    .error(R.drawable.ic_tv)
                    .into(holder.imgThumb);
        } else {
            holder.imgThumb.setImageResource(R.drawable.ic_tv);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTarget;
        ImageView imgThumb;
        ImageButton imgEdit, imgDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTarget = itemView.findViewById(R.id.tvTarget);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}
