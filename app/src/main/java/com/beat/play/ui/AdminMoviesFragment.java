package com.beat.play.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beat.play.R;
import com.beat.play.adapter.AdminMovieAdapter;
import com.beat.play.data.DataStore;
import com.beat.play.model.Movie;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminMoviesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private AdminMovieAdapter adapter;
    private DatabaseReference reference;
    private ValueEventListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        FloatingActionButton fab = view.findViewById(R.id.fab);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminMovieAdapter();
        adapter.setOnAction(new AdminMovieAdapter.OnAction() {
            @Override
            public void onEdit(Movie movie) {
                showMovieDialog(movie);
            }

            @Override
            public void onDelete(Movie movie) {
                confirmDelete(movie);
            }
        });
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> showAddOptions());
    }

    private void showAddOptions() {
        String[] options = {"একটি মুভি যোগ করুন", "বাল্ক আপলোড"};
        new AlertDialog.Builder(requireContext())
                .setTitle("যোগ করুন")
                .setItems(options, (d, which) -> {
                    if (which == 0) {
                        showMovieDialog(null);
                    } else {
                        showBulkMovieDialog();
                    }
                })
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();
        reference = DataStore.movies();
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Movie> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Movie movie = child.getValue(Movie.class);
                    if (movie != null) {
                        movie.id = child.getKey();
                        list.add(movie);
                    }
                }
                adapter.setData(list);
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        };
        reference.addValueEventListener(listener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (reference != null && listener != null) {
            reference.removeEventListener(listener);
        }
    }

    private void showMovieDialog(Movie existing) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_movie, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etUrl = dialogView.findViewById(R.id.etUrl);
        EditText etThumbnail = dialogView.findViewById(R.id.etThumbnail);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etYear = dialogView.findViewById(R.id.etYear);
        EditText etCategory = dialogView.findViewById(R.id.etCategory);

        boolean isEdit = existing != null;
        if (isEdit) {
            etTitle.setText(existing.title);
            etUrl.setText(existing.url);
            etThumbnail.setText(existing.thumbnail);
            etDescription.setText(existing.description);
            etYear.setText(existing.year);
            etCategory.setText(existing.category);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? "মুভি সম্পাদনা" : "নতুন মুভি")
                .setView(dialogView)
                .setPositiveButton("সেভ", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String url = etUrl.getText().toString().trim();
                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(url)) {
                        Toast.makeText(requireContext(), "নাম ও লিংক বাধ্যতামূলক", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> values = new HashMap<>();
                    values.put("title", title);
                    values.put("url", url);
                    values.put("thumbnail", etThumbnail.getText().toString().trim());
                    values.put("description", etDescription.getText().toString().trim());
                    values.put("year", etYear.getText().toString().trim());
                    values.put("category", etCategory.getText().toString().trim());

                    if (isEdit) {
                        reference.child(existing.id).updateChildren(values);
                    } else {
                        reference.push().setValue(values);
                    }
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void showBulkMovieDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bulk_movie, null);
        EditText etBulk = dialogView.findViewById(R.id.etBulk);

        new AlertDialog.Builder(requireContext())
                .setTitle("বাল্ক আপলোড")
                .setMessage("প্রতিটি লাইনে: নাম | লিংক | থাম্বনেইল | সাল | ক্যাটাগরি")
                .setView(dialogView)
                .setPositiveButton("আপলোড", (d, w) -> {
                    int count = uploadBulkMovies(etBulk.getText().toString());
                    Toast.makeText(requireContext(), count + "টি মুভি যোগ হয়েছে", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private int uploadBulkMovies(String text) {
        int count = 0;
        for (String line : text.split("\n")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                continue;
            }
            String title = parts[0].trim();
            String url = parts[1].trim();
            if (title.isEmpty() || url.isEmpty()) {
                continue;
            }
            Map<String, Object> values = new HashMap<>();
            values.put("title", title);
            values.put("url", url);
            values.put("thumbnail", parts.length > 2 ? parts[2].trim() : "");
            values.put("year", parts.length > 3 ? parts[3].trim() : "");
            values.put("category", parts.length > 4 ? parts[4].trim() : "");
            reference.push().setValue(values);
            count++;
        }
        return count;
    }

    private void confirmDelete(Movie movie) {
        new AlertDialog.Builder(requireContext())
                .setTitle("মুছুন")
                .setMessage("\"" + movie.title + "\" মুছে ফেলবেন?")
                .setPositiveButton("হ্যাঁ", (d, w) -> reference.child(movie.id).removeValue())
                .setNegativeButton("না", null)
                .show();
    }
}
