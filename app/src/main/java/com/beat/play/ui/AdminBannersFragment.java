package com.beat.play.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beat.play.R;
import com.beat.play.adapter.AdminBannerAdapter;
import com.beat.play.data.DataStore;
import com.beat.play.model.Banner;
import com.beat.play.model.Channel;
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

public class AdminBannersFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private AdminBannerAdapter adapter;
    private DatabaseReference reference;
    private ValueEventListener listener;

    private final List<Channel> channelsList = new ArrayList<>();
    private final List<Movie> moviesList = new ArrayList<>();

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
        adapter = new AdminBannerAdapter();
        adapter.setOnAction(new AdminBannerAdapter.OnAction() {
            @Override
            public void onEdit(Banner banner) {
                openBannerDialog(banner);
            }

            @Override
            public void onDelete(Banner banner) {
                confirmDelete(banner);
            }
        });
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> openBannerDialog(null));
    }

    @Override
    public void onStart() {
        super.onStart();
        reference = DataStore.banners();
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Banner> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Banner banner = child.getValue(Banner.class);
                    if (banner != null) {
                        banner.id = child.getKey();
                        list.add(banner);
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

    private void openBannerDialog(Banner existing) {
        channelsList.clear();
        moviesList.clear();

        DataStore.channels().get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful() && task1.getResult() != null) {
                for (DataSnapshot child : task1.getResult().getChildren()) {
                    Channel channel = child.getValue(Channel.class);
                    if (channel != null) {
                        channel.id = child.getKey();
                        channelsList.add(channel);
                    }
                }
            }
            DataStore.movies().get().addOnCompleteListener(task2 -> {
                if (task2.isSuccessful() && task2.getResult() != null) {
                    for (DataSnapshot child : task2.getResult().getChildren()) {
                        Movie movie = child.getValue(Movie.class);
                        if (movie != null) {
                            movie.id = child.getKey();
                            moviesList.add(movie);
                        }
                    }
                }
                buildAndShowBannerDialog(existing);
            });
        });
    }

    private void buildAndShowBannerDialog(Banner existing) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_banner, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etImage = dialogView.findViewById(R.id.etImage);
        Spinner spType = dialogView.findViewById(R.id.spType);
        Spinner spTarget = dialogView.findViewById(R.id.spTarget);

        String[] types = {"চ্যানেল", "মুভি"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                populateTarget(spTarget, position == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (existing != null) {
            etTitle.setText(existing.title);
            etImage.setText(existing.image);
            int channelIndex = indexOfChannel(existing.targetUrl);
            if (channelIndex >= 0) {
                spType.setSelection(0);
                populateTarget(spTarget, true);
                if (channelIndex < channelsList.size()) {
                    spTarget.setSelection(channelIndex);
                }
            } else {
                spType.setSelection(1);
                populateTarget(spTarget, false);
                int movieIndex = indexOfMovie(existing.targetUrl);
                if (movieIndex >= 0 && movieIndex < moviesList.size()) {
                    spTarget.setSelection(movieIndex);
                }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing != null ? "ব্যানার সম্পাদনা" : "নতুন ব্যানার")
                .setView(dialogView)
                .setPositiveButton("সেভ", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String image = etImage.getText().toString().trim();
                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(image)) {
                        Toast.makeText(requireContext(), "নাম ও ইমেজ URL বাধ্যতামূলক", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String targetTitle = null;
                    String targetUrl = null;
                    int pos = spTarget.getSelectedItemPosition();
                    boolean isChannel = spType.getSelectedItemPosition() == 0;
                    if (isChannel && pos >= 0 && pos < channelsList.size()) {
                        targetTitle = channelsList.get(pos).name;
                        targetUrl = channelsList.get(pos).url;
                    } else if (!isChannel && pos >= 0 && pos < moviesList.size()) {
                        targetTitle = moviesList.get(pos).title;
                        targetUrl = moviesList.get(pos).url;
                    }

                    Map<String, Object> values = new HashMap<>();
                    values.put("title", title);
                    values.put("image", image);
                    values.put("targetTitle", targetTitle);
                    values.put("targetUrl", targetUrl);

                    if (existing != null) {
                        reference.child(existing.id).updateChildren(values);
                    } else {
                        reference.push().setValue(values);
                    }
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void populateTarget(Spinner spTarget, boolean isChannel) {
        List<String> labels = new ArrayList<>();
        if (isChannel) {
            for (Channel channel : channelsList) {
                labels.add(channel.name);
            }
        } else {
            for (Movie movie : moviesList) {
                labels.add(movie.title);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTarget.setAdapter(adapter);
    }

    private int indexOfChannel(String url) {
        if (url == null) {
            return -1;
        }
        for (int i = 0; i < channelsList.size(); i++) {
            if (url.equals(channelsList.get(i).url)) {
                return i;
            }
        }
        return -1;
    }

    private int indexOfMovie(String url) {
        if (url == null) {
            return -1;
        }
        for (int i = 0; i < moviesList.size(); i++) {
            if (url.equals(moviesList.get(i).url)) {
                return i;
            }
        }
        return -1;
    }

    private void confirmDelete(Banner banner) {
        new AlertDialog.Builder(requireContext())
                .setTitle("মুছুন")
                .setMessage("\"" + banner.title + "\" মুছে ফেলবেন?")
                .setPositiveButton("হ্যাঁ", (d, w) -> reference.child(banner.id).removeValue())
                .setNegativeButton("না", null)
                .show();
    }
}
