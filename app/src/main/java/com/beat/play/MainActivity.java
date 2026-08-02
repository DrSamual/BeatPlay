package com.beat.play;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.beat.play.ui.AnnouncementsFragment;
import com.beat.play.ui.ChannelsFragment;
import com.beat.play.ui.HomeFragment;
import com.beat.play.ui.MoviesFragment;
import com.beat.play.ui.SettingsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);
        viewPager = findViewById(R.id.viewPager);
        bottomNav = findViewById(R.id.bottomNav);

        viewPager.setAdapter(new MainPagerAdapter(this));
        viewPager.setOffscreenPageLimit(4);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                viewPager.setCurrentItem(0, false);
            } else if (id == R.id.nav_live) {
                viewPager.setCurrentItem(1, false);
            } else if (id == R.id.nav_movies) {
                viewPager.setCurrentItem(2, false);
            } else if (id == R.id.nav_announcements) {
                viewPager.setCurrentItem(3, false);
            } else if (id == R.id.nav_settings) {
                viewPager.setCurrentItem(4, false);
            }
            return true;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                int itemId;
                switch (position) {
                    case 0:
                        itemId = R.id.nav_home;
                        break;
                    case 1:
                        itemId = R.id.nav_live;
                        break;
                    case 2:
                        itemId = R.id.nav_movies;
                        break;
                    case 3:
                        itemId = R.id.nav_announcements;
                        break;
                    default:
                        itemId = R.id.nav_settings;
                        break;
                }
                bottomNav.setSelectedItemId(itemId);
                navView.setCheckedItem(itemId);
            }
        });

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

        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                viewPager.setCurrentItem(0, false);
            } else if (id == R.id.nav_live) {
                viewPager.setCurrentItem(1, false);
            } else if (id == R.id.nav_movies) {
                viewPager.setCurrentItem(2, false);
            } else if (id == R.id.nav_announcements) {
                viewPager.setCurrentItem(3, false);
            } else if (id == R.id.nav_settings) {
                viewPager.setCurrentItem(4, false);
            } else if (id == R.id.nav_admin) {
                startActivity(new Intent(this, LoginActivity.class));
            } else if (id == R.id.nav_share) {
                shareApp();
            } else if (id == R.id.nav_about) {
                Toast.makeText(this, "BeatPlay — লাইভ টিভি ও মুভি স্ট্রিমিং অ্যাপ", Toast.LENGTH_LONG).show();
            }
            return true;
        });
    }

    public void openTab(int position) {
        if (viewPager != null) {
            viewPager.setCurrentItem(position, false);
        }
    }

    private void shareApp() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        send.putExtra(Intent.EXTRA_TEXT,
                "BeatPlay অ্যাপটি দেখুন — লাইভ টিভি ও মুভি স্ট্রিমিং!");
        startActivity(Intent.createChooser(send, "অ্যাপ শেয়ার করুন"));
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
                case 2:
                    return new MoviesFragment();
                case 3:
                    return new AnnouncementsFragment();
                default:
                    return new SettingsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 5;
        }
    }
}
