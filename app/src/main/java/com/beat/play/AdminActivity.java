package com.beat.play;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.beat.play.ui.AdminBannersFragment;
import com.beat.play.ui.AdminChannelsFragment;
import com.beat.play.ui.AdminMoviesFragment;
import com.beat.play.ui.AdminNotificationsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("অ্যাডমিন প্যানেল");
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new AdminPagerAdapter(this));

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("চ্যানেল");
                    break;
                case 1:
                    tab.setText("মুভি");
                    break;
                case 2:
                    tab.setText("ব্যানার");
                    break;
                default:
                    tab.setText("ঘোষণা");
                    break;
            }
        }).attach();
    }

    private static class AdminPagerAdapter extends FragmentStateAdapter {

        AdminPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new AdminChannelsFragment();
                case 1:
                    return new AdminMoviesFragment();
                case 2:
                    return new AdminBannersFragment();
                default:
                    return new AdminNotificationsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
