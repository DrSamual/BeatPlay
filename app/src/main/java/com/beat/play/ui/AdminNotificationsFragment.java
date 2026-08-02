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
import com.beat.play.adapter.AdminAnnouncementAdapter;
import com.beat.play.data.DataStore;
import com.beat.play.model.Announcement;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminNotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private AdminAnnouncementAdapter adapter;
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
        adapter = new AdminAnnouncementAdapter();
        adapter.setOnAction(new AdminAnnouncementAdapter.OnAction() {
            @Override
            public void onEdit(Announcement announcement) {
                showAnnouncementDialog(announcement);
            }

            @Override
            public void onDelete(Announcement announcement) {
                confirmDelete(announcement);
            }
        });
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> showAnnouncementDialog(null));
    }

    @Override
    public void onStart() {
        super.onStart();
        reference = DataStore.notifications();
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Announcement> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Announcement announcement = child.getValue(Announcement.class);
                    if (announcement != null) {
                        announcement.id = child.getKey();
                        list.add(announcement);
                    }
                }
                Collections.sort(list, (a, b) -> Long.compare(b.timestamp, a.timestamp));
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

    private void showAnnouncementDialog(Announcement existing) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_announcement, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etMessage = dialogView.findViewById(R.id.etMessage);

        boolean isEdit = existing != null;
        if (isEdit) {
            etTitle.setText(existing.title);
            etMessage.setText(existing.message);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? "ঘোষণা সম্পাদনা" : "নতুন ঘোষণা")
                .setView(dialogView)
                .setPositiveButton("সেভ", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String message = etMessage.getText().toString().trim();
                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(requireContext(), "শিরোনাম বাধ্যতামূলক", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> values = new HashMap<>();
                    values.put("title", title);
                    values.put("message", message);
                    if (isEdit) {
                        reference.child(existing.id).updateChildren(values);
                    } else {
                        values.put("timestamp", System.currentTimeMillis());
                        reference.push().setValue(values);
                    }
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void confirmDelete(Announcement announcement) {
        new AlertDialog.Builder(requireContext())
                .setTitle("মুছুন")
                .setMessage("\"" + announcement.title + "\" মুছে ফেলবেন?")
                .setPositiveButton("হ্যাঁ", (d, w) -> reference.child(announcement.id).removeValue())
                .setNegativeButton("না", null)
                .show();
    }
}
