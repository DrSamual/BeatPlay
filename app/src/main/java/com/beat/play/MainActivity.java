package com.beat.play;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.beat.play.ui.ChannelsFragment;
import com.beat.play.ui.HomeFragment;
import com.beat.play.ui.MoviesFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new MainPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                viewPager.setCurrentItem(0, false);
            } else if (id == R.id.nav_live) {
                viewPager.setCurrentItem(1, false);
            } else if (id == R.id.nav_movies) {
                viewPager.setCurrentItem(2, false);
            }
            return true;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                int itemId;
                if (position == 0) {
                    itemId = R.id.nav_home;
                } else if (position == 1) {
                    itemId = R.id.nav_live;
                } else {
                    itemId = R.id.nav_movies;
                }
                bottomNav.setSelectedItemId(itemId);
            }
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_notifications) {
                startActivity(AnnouncementsActivity.intent(this));
                return true;
            } else if (id == R.id.action_admin) {
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            }
            return false;
        });
    }

    private static class MainPagerAdapter extends FragmentStateAdapter {

        MainPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new ChannelsFragment();
                default:
                    return new MoviesFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
