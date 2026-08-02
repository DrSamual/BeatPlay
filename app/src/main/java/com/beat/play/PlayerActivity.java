package com.beat.play;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_URL = "extra_url";

    private PlayerView playerView;
    private ExoPlayer player;
    private ProgressBar progressBar;

    public static void start(Context context, String title, String url) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_URL, url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        hideSystemBars();

        playerView = findViewById(R.id.playerView);
        progressBar = findViewById(R.id.progressBar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        ImageButton btnBack = findViewById(R.id.btnBack);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String url = getIntent().getStringExtra(EXTRA_URL);
        tvTitle.setText(title != null ? title : "");
        btnBack.setOnClickListener(v -> finish());

        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "ভুল স্ট্রিম লিংক", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        initializePlayer(url);
    }

    private void initializePlayer(String url) {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setMediaItem(MediaItem.fromUri(Uri.parse(url)));
        player.setPlayWhenReady(true);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PlayerActivity.this,
                        "স্ট্রিম চালু করা যায়নি। লিংকটি পরীক্ষা করুন।", Toast.LENGTH_LONG).show();
            }
        });
        player.prepare();
    }

    private void hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
