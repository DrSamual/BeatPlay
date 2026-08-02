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
import com.beat.play.adapter.AdminChannelAdapter;
import com.beat.play.data.DataStore;
import com.beat.play.model.Channel;
import com.beat.play.util.HttpUtil;
import com.beat.play.util.PlaylistParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminChannelsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private AdminChannelAdapter adapter;
    private DatabaseReference reference;
    private ValueEventListener listener;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
        adapter = new AdminChannelAdapter();
        adapter.setOnAction(new AdminChannelAdapter.OnAction() {
            @Override
            public void onEdit(Channel channel) {
                showChannelDialog(channel);
            }

            @Override
            public void onDelete(Channel channel) {
                confirmDelete(channel);
            }
        });
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> showAddOptions());
    }

    private void showAddOptions() {
        String[] options = {"একটি চ্যানেল যোগ করুন", "বাল্ক আপলোড", "প্লে লিস্ট ইমপোর্ট"};
        new AlertDialog.Builder(requireContext())
                .setTitle("যোগ করুন")
                .setItems(options, (d, which) -> {
                    if (which == 0) {
                        showChannelDialog(null);
                    } else if (which == 1) {
                        showBulkChannelDialog();
                    } else {
                        showPlaylistDialog();
                    }
                })
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();
        reference = DataStore.channels();
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Channel> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Channel channel = child.getValue(Channel.class);
                    if (channel != null) {
                        channel.id = child.getKey();
                        list.add(channel);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void showChannelDialog(Channel existing) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_channel, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etUrl = dialogView.findViewById(R.id.etUrl);
        EditText etLogo = dialogView.findViewById(R.id.etLogo);
        EditText etCategory = dialogView.findViewById(R.id.etCategory);

        boolean isEdit = existing != null;
        if (isEdit) {
            etName.setText(existing.name);
            etUrl.setText(existing.url);
            etLogo.setText(existing.logo);
            etCategory.setText(existing.category);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? "চ্যানেল সম্পাদনা" : "নতুন চ্যানেল")
                .setView(dialogView)
                .setPositiveButton("সেভ", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String url = etUrl.getText().toString().trim();
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(url)) {
                        Toast.makeText(requireContext(), "নাম ও লিংক বাধ্যতামূলক", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> values = new HashMap<>();
                    values.put("name", name);
                    values.put("url", url);
                    values.put("logo", etLogo.getText().toString().trim());
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

    private void showBulkChannelDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bulk_channel, null);
        EditText etBulk = dialogView.findViewById(R.id.etBulk);

        new AlertDialog.Builder(requireContext())
                .setTitle("বাল্ক আপলোড")
                .setMessage("প্রতিটি লাইনে: নাম | লিংক | ক্যাটাগরি | লোগো")
                .setView(dialogView)
                .setPositiveButton("আপলোড", (d, w) -> {
                    int count = uploadBulkChannels(etBulk.getText().toString());
                    Toast.makeText(requireContext(), count + "টি চ্যানেল যোগ হয়েছে", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private int uploadBulkChannels(String text) {
        int count = 0;
        for (String line : text.split("\n")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                continue;
            }
            String name = parts[0].trim();
            String url = parts[1].trim();
            if (name.isEmpty() || url.isEmpty()) {
                continue;
            }
            Map<String, Object> values = new HashMap<>();
            values.put("name", name);
            values.put("url", url);
            values.put("category", parts.length > 2 ? parts[2].trim() : "");
            values.put("logo", parts.length > 3 ? parts[3].trim() : "");
            reference.push().setValue(values);
            count++;
        }
        return count;
    }

    private void showPlaylistDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_playlist, null);
        EditText etUrl = dialogView.findViewById(R.id.etUrl);
        EditText etRaw = dialogView.findViewById(R.id.etRaw);

        new AlertDialog.Builder(requireContext())
                .setTitle("প্লে লিস্ট ইমপোর্ট")
                .setMessage("M3U URL দিন বা টেক্সট পেস্ট করুন")
                .setView(dialogView)
                .setPositiveButton("ইমপোর্ট", (d, w) -> {
                    String url = etUrl.getText().toString().trim();
                    String raw = etRaw.getText().toString().trim();
                    if (url.isEmpty() && raw.isEmpty()) {
                        Toast.makeText(requireContext(), "URL বা টেক্সট দিন", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    importPlaylist(url, raw);
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void importPlaylist(String url, String raw) {
        AlertDialog progress = new AlertDialog.Builder(requireContext())
                .setTitle("ইমপোর্ট হচ্ছে...")
                .setView(new ProgressBar(requireContext()))
                .setCancelable(false)
                .create();
        progress.show();

        executor.execute(() -> {
            String content = raw;
            if (!url.isEmpty()) {
                try {
                    content = HttpUtil.get(url);
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        progress.dismiss();
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "প্লে লিস্ট লোড করা যায়নি", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
            }
            List<Channel> channels = PlaylistParser.parse(content);
            requireActivity().runOnUiThread(() -> {
                progress.dismiss();
                if (!isAdded()) {
                    return;
                }
                for (Channel channel : channels) {
                    Map<String, Object> values = new HashMap<>();
                    values.put("name", channel.name);
                    values.put("url", channel.url);
                    values.put("logo", channel.logo);
                    values.put("category", channel.category);
                    reference.push().setValue(values);
                }
                Toast.makeText(requireContext(), channels.size() + "টি চ্যানেল ইমপোর্ট হয়েছে",
                        Toast.LENGTH_LONG).show();
            });
        });
    }

    private void confirmDelete(Channel channel) {
        new AlertDialog.Builder(requireContext())
                .setTitle("মুছুন")
                .setMessage("\"" + channel.name + "\" মুছে ফেলবেন?")
                .setPositiveButton("হ্যাঁ", (d, w) -> reference.child(channel.id).removeValue())
                .setNegativeButton("না", null)
                .show();
    }
}
