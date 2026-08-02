package com.beat.play;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beat.play.adapter.AnnouncementAdapter;
import com.beat.play.data.DataStore;
import com.beat.play.model.Announcement;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnnouncementsActivity extends AppCompatActivity {

    public static Intent intent(Context context) {
        return new Intent(context, AnnouncementsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView emptyView = findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AnnouncementAdapter adapter = new AnnouncementAdapter();
        recyclerView.setAdapter(adapter);

        DatabaseReference reference = DataStore.notifications();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
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
                emptyView.setVisibility(list.isEmpty() ? android.view.View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AnnouncementsActivity.this, "ডেটা লোড করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
