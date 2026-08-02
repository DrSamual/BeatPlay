package com.beat.play.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beat.play.R;
import com.beat.play.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class AdminMovieAdapter extends RecyclerView.Adapter<AdminMovieAdapter.VH> {

    public interface OnAction {
        void onEdit(Movie movie);

        void onDelete(Movie movie);
    }

    private final List<Movie> data = new ArrayList<>();
    private OnAction listener;

    public void setOnAction(OnAction listener) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_movie, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Movie movie = data.get(position);
        holder.tvTitle.setText(movie.title != null ? movie.title : "");
        holder.tvYear.setText(movie.year != null ? movie.year : "");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(movie);
            }
        });
        holder.imgEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(movie);
            }
        });
        holder.imgDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvYear;
        ImageButton imgEdit, imgDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvYear = itemView.findViewById(R.id.tvYear);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}
