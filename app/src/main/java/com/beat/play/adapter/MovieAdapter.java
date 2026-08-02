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
import com.beat.play.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.VH> {

    public interface OnItemClick {
        void onClick(Movie movie);
    }

    private final List<Movie> data = new ArrayList<>();
    private OnItemClick listener;

    public void setOnItemClickListener(OnItemClick listener) {
        this.listener = listener;
    }

    public void setData(List<Movie> movies) {
        data.clear();
        data.addAll(movies);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Movie movie = data.get(position);
        holder.tvTitle.setText(movie.title != null ? movie.title : "");
        holder.tvYear.setText(movie.year != null ? movie.year : "");
        holder.tvCategory.setText(movie.category != null ? movie.category : "");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(movie);
            }
        });

        if (movie.thumbnail != null && !movie.thumbnail.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(movie.thumbnail)
                    .placeholder(R.drawable.ic_tv)
                    .error(R.drawable.ic_tv)
                    .into(holder.imgThumbnail);
        } else {
            holder.imgThumbnail.setImageResource(R.drawable.ic_tv);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvYear, tvCategory;
        ImageView imgThumbnail;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
        }
    }
}
