package com.beat.play.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.beat.play.AnnouncementsActivity;
import com.beat.play.PlayerActivity;
import com.beat.play.R;
import com.beat.play.adapter.BannerAdapter;
import com.beat.play.adapter.HomeChannelAdapter;
import com.beat.play.adapter.HomeMovieAdapter;
import com.beat.play.data.DataStore;
import com.beat.play.model.Announcement;
import com.beat.play.model.Banner;
import com.beat.play.model.Channel;
import com.beat.play.model.Movie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 bannerPager;
    private BannerAdapter bannerAdapter;
    private LinearLayout dotsLayout;
    private RecyclerView rvChannels;
    private RecyclerView rvMovies;
    private LinearLayout announcementContainer;
    private TextView tvAnnouncementsMore;

    private HomeChannelAdapter channelAdapter;
    private HomeMovieAdapter movieAdapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoScroll = new Runnable() {
        @Override
        public void run() {
            if (bannerAdapter != null && bannerAdapter.getItemCount() > 0) {
                bannerPager.setCurrentItem(bannerPager.getCurrentItem() + 1, true);
                handler.postDelayed(this, 4000);
            }
        }
    };

    private final List<DatabaseReference> refs = new ArrayList<>();
    private final List<ValueEventListener> listeners = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bannerPager = view.findViewById(R.id.bannerPager);
        dotsLayout = view.findViewById(R.id.dotsLayout);
        rvChannels = view.findViewById(R.id.rvChannels);
        rvMovies = view.findViewById(R.id.rvMovies);
        announcementContainer = view.findViewById(R.id.announcementContainer);
        tvAnnouncementsMore = view.findViewById(R.id.tvAnnouncementsMore);

        bannerAdapter = new BannerAdapter();
        bannerAdapter.setOnItemClickListener(banner -> {
            if (banner.targetUrl != null) {
                PlayerActivity.start(requireContext(), banner.targetTitle, banner.targetUrl);
            }
        });
        bannerPager.setAdapter(bannerAdapter);
        bannerPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float abs = Math.abs(position);
                page.setScaleX(1f - 0.08f * abs);
                page.setScaleY(1f - 0.08f * abs);
                page.setAlpha(1f - 0.35f * abs);
            }
        });
        bannerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });

        channelAdapter = new HomeChannelAdapter();
        channelAdapter.setOnItemClickListener(channel -> {
            if (channel.url != null) {
                PlayerActivity.start(requireContext(), channel.name, channel.url);
            }
        });
        rvChannels.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        rvChannels.setAdapter(channelAdapter);

        movieAdapter = new HomeMovieAdapter();
        movieAdapter.setOnItemClickListener(movie -> {
            if (movie.url != null) {
                PlayerActivity.start(requireContext(), movie.title, movie.url);
            }
        });
        rvMovies.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        rvMovies.setAdapter(movieAdapter);

        tvAnnouncementsMore.setOnClickListener(v ->
                startActivity(AnnouncementsActivity.intent(requireContext())));
    }

    @Override
    public void onStart() {
        super.onStart();
        loadBanners();
        loadAnnouncements();
        loadChannels();
        loadMovies();
    }

    private void loadBanners() {
        addListener(DataStore.banners(), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Banner> banners = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Banner banner = child.getValue(Banner.class);
                    if (banner != null) {
                        banner.id = child.getKey();
                        banners.add(banner);
                    }
                }
                bannerAdapter.setData(banners);
                setupDots(banners.size());
                if (!banners.isEmpty()) {
                    bannerPager.setCurrentItem(banners.size() * 500, false);
                    startAutoScroll();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadAnnouncements() {
        addListener(DataStore.notifications(), new ValueEventListener() {
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

                announcementContainer.removeAllViews();
                int count = Math.min(3, list.size());
                for (int i = 0; i < count; i++) {
                    Announcement item = list.get(i);
                    View card = LayoutInflater.from(requireContext())
                            .inflate(R.layout.item_announcement, announcementContainer, false);
                    ((TextView) card.findViewById(R.id.tvTitle)).setText(item.title);
                    ((TextView) card.findViewById(R.id.tvMessage)).setText(item.message);
                    ((TextView) card.findViewById(R.id.tvTime)).setText(Announcement.formatTime(item.timestamp));
                    announcementContainer.addView(card);
                }
                tvAnnouncementsMore.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadChannels() {
        addListener(DataStore.channels(), new ValueEventListener() {
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
                if (channels.size() > 10) {
                    channels = channels.subList(0, 10);
                }
                channelAdapter.setData(channels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadMovies() {
        addListener(DataStore.movies(), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Movie> movies = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Movie movie = child.getValue(Movie.class);
                    if (movie != null) {
                        movie.id = child.getKey();
                        movies.add(movie);
                    }
                }
                if (movies.size() > 10) {
                    movies = movies.subList(0, 10);
                }
                movieAdapter.setData(movies);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupDots(int count) {
        dotsLayout.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(10, 10);
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_inactive);
            dotsLayout.addView(dot);
        }
        updateDots(0);
    }

    private void updateDots(int position) {
        if (bannerAdapter.getData().isEmpty()) {
            return;
        }
        int real = position % bannerAdapter.getData().size();
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            dotsLayout.getChildAt(i).setBackgroundResource(
                    i == real ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void startAutoScroll() {
        handler.removeCallbacks(autoScroll);
        handler.postDelayed(autoScroll, 4000);
    }

    private void addListener(DatabaseReference ref, ValueEventListener listener) {
        refs.add(ref);
        listeners.add(listener);
        ref.addValueEventListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bannerAdapter != null && bannerAdapter.getItemCount() > 0) {
            startAutoScroll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(autoScroll);
    }

    @Override
    public void onStop() {
        super.onStop();
        for (int i = 0; i < refs.size(); i++) {
            refs.get(i).removeEventListener(listeners.get(i));
        }
        refs.clear();
        listeners.clear();
    }
}
