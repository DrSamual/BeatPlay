package com.beat.play.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beat.play.PlayerActivity;
import com.beat.play.R;
import com.beat.play.adapter.ChannelAdapter;
import com.beat.play.data.DataStore;
import com.beat.play.model.Channel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChannelsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private ChannelAdapter adapter;
    private DatabaseReference reference;
    private ValueEventListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ChannelAdapter();
        adapter.setOnItemClickListener(channel -> {
            if (channel.url != null) {
                PlayerActivity.start(requireContext(), channel.name, channel.url);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadChannels();
    }

    private void loadChannels() {
        reference = DataStore.channels();
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Channel> channels = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Channel channel = child.getValue(Channel.class);
                    if (channel != null) {
                        channel.id = child.getKey();
                        channels.add(channel);
                    }
                }
                adapter.setData(channels);
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(channels.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "ডেটা লোড করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show();
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
}
