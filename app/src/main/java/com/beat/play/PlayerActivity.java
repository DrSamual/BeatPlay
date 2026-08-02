package com.beat.play;

import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.ui.PlayerView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_URL = "extra_url";

    private static final Pattern YOUTUBE_ID =
            Pattern.compile("(?:youtube\\.com/(?:watch\\?v=|shorts/|embed/|v/)|youtu\\.be/)([\\w-]{11})");

    private PlayerView playerView;
    private WebView webView;
    private ExoPlayer player;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private LinearLayout topBar;
    private ImageButton btnFullscreen;
    private FrameLayout webViewContainer;

    private String mediaUrl;
    private boolean isYouTube = false;
    private boolean isFullscreen = false;
    private boolean isPip = false;
    private int videoWidth = 0;
    private int videoHeight = 0;

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

        playerView = findViewById(R.id.playerView);
        webView = findViewById(R.id.webView);
        webViewContainer = findViewById(R.id.webViewContainer);
        progressBar = findViewById(R.id.progressBar);
        errorLayout = findViewById(R.id.errorLayout);
        topBar = findViewById(R.id.topBar);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        TextView tvTitle = findViewById(R.id.tvTitle);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnPip = findViewById(R.id.btnPip);
        ImageButton btnRetry = findViewById(R.id.btnRetry);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        mediaUrl = getIntent().getStringExtra(EXTRA_URL);
        tvTitle.setText(title != null ? title : "");

        btnBack.setOnClickListener(v -> finish());

        btnFullscreen.setOnClickListener(v -> toggleFullscreen());

        btnPip.setOnClickListener(v -> enterPipMode());

        btnRetry.setOnClickListener(v -> {
            errorLayout.setVisibility(View.GONE);
            initializeMedia();
        });

        if (mediaUrl == null || mediaUrl.isEmpty()) {
            showError("ভুল স্ট্রিম লিংক");
            return;
        }

        initializeMedia();
    }

    private void initializeMedia() {
        if (isYouTubeUrl(mediaUrl)) {
            isYouTube = true;
            playerView.setVisibility(View.GONE);
            webViewContainer.setVisibility(View.VISIBLE);
            webView.setVisibility(View.VISIBLE);
            setupWebView(extractYoutubeId(mediaUrl));
        } else {
            isYouTube = false;
            webViewContainer.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            initializePlayer(mediaUrl);
        }
    }

    private void initializePlayer(String url) {
        DefaultRenderersFactory renderersFactory =
                new DefaultRenderersFactory(this)
                        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);

        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                        60_000,
                        180_000,
                        5_000,
                        10_000)
                .build();

        player = new ExoPlayer.Builder(this)
                .setRenderersFactory(renderersFactory)
                .setLoadControl(loadControl)
                .build();
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
            public void onVideoSizeChanged(@NonNull VideoSize videoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    videoWidth = videoSize.width;
                    videoHeight = videoSize.height;
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                progressBar.setVisibility(View.GONE);
                showError("স্ট্রিম চালু করা যায়নি।\nলিংকটি পরীক্ষা করুন।");
            }
        });
        player.prepare();
    }

    private boolean isYouTubeUrl(String url) {
        if (url == null) {
            return false;
        }
        String lower = url.toLowerCase();
        return lower.contains("youtube.com") || lower.contains("youtu.be");
    }

    private String extractYoutubeId(String url) {
        Matcher matcher = YOUTUBE_ID.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return url;
    }

    private void setupWebView(String videoId) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.youtube.com/embed/" + videoId
                + "?autoplay=1&rel=0&playsinline=1");
    }

    private void toggleFullscreen() {
        if (isPip) {
            return;
        }
        isFullscreen = !isFullscreen;
        if (isFullscreen) {
            hideSystemBars();
            topBar.setVisibility(View.GONE);
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
        } else {
            showSystemBars();
            topBar.setVisibility(View.VISIBLE);
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen);
        }
    }

    private void enterPipMode() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(this, "এই ডিভাইসে পিপ মোড সাপোর্টেড নয়", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isInPictureInPictureMode()) {
            return;
        }
        int w = videoWidth > 0 ? videoWidth : 16;
        int h = videoHeight > 0 ? videoHeight : 9;
        PictureInPictureParams params = new PictureInPictureParams.Builder()
                .setAspectRatio(new Rational(w, h))
                .build();
        enterPictureInPictureMode(params);
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

    private void showSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.show(WindowInsetsCompat.Type.systemBars());
        }
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (isYouTube && !isInPictureInPictureMode()) {
            enterPipMode();
        }
    }

    @Override
    protected void onPictureInPictureModeChanged(boolean isInPictureInPictureMode,
                                                 @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        isPip = isInPictureInPictureMode;
        if (isPip) {
            topBar.setVisibility(View.GONE);
            if (player != null) {
                player.setPlayWhenReady(true);
            }
        } else {
            topBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && !isPip) {
            player.setPlayWhenReady(false);
        }
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen && !isPip) {
            toggleFullscreen();
        } else if (webView != null && webView.canGoBack() && isYouTube) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
        if (webView != null) {
            webView.destroy();
        }
    }
}
