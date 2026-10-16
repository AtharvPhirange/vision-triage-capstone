package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class VideoPlayerActivity extends BaseActivity {

    public static final String EXTRA_VIDEO_ID = "extra_video_id";
    public static final String EXTRA_VIDEO_TITLE = "extra_video_title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        String videoId = getIntent().getStringExtra(EXTRA_VIDEO_ID);
        String title = getIntent().getStringExtra(EXTRA_VIDEO_TITLE);

        TextView tvTitle = findViewById(R.id.tvPlayerTitle);
        if (tvTitle != null && title != null) {
            tvTitle.setText(title);
        }

        YouTubePlayerView playerView = findViewById(R.id.youtube_player_view);
        if (playerView != null) {
            getLifecycle().addObserver(playerView);
            playerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    if (videoId != null && !videoId.trim().isEmpty()) {
                        youTubePlayer.loadVideo(videoId, 0);
                    }
                }
            });
        }

        findViewById(R.id.btnClosePlayer).setOnClickListener(v -> finish());
    }
}