package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class VideoPlayerDialogFragment extends DialogFragment {

    private static final String ARG_VIDEO_ID = "video_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";

    public static VideoPlayerDialogFragment newInstance(String videoId, String title, String desc) {
        VideoPlayerDialogFragment fragment = new VideoPlayerDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_ID, videoId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESC, desc);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_in_app_video_player, container, false);

        YouTubePlayerView youTubePlayerView = view.findViewById(R.id.youtubePlayerView);
        TextView tvVideoTitle = view.findViewById(R.id.tvVideoTitle);
        TextView tvVideoDescription = view.findViewById(R.id.tvVideoDescription);

        String videoId = getArguments() != null ? getArguments().getString(ARG_VIDEO_ID) : "";
        String title = getArguments() != null ? getArguments().getString(ARG_TITLE) : "";
        String desc = getArguments() != null ? getArguments().getString(ARG_DESC) : "";

        if (tvVideoTitle != null && title != null) tvVideoTitle.setText(title);
        if (tvVideoDescription != null && desc != null) tvVideoDescription.setText(desc);

        if (youTubePlayerView != null) {
            getViewLifecycleOwner().getLifecycle().addObserver(youTubePlayerView);
            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    if (videoId != null && !videoId.trim().isEmpty()) {
                        youTubePlayer.loadVideo(videoId, 0);
                    }
                }
            });
        }

        View btnClosePlayer = view.findViewById(R.id.btnClosePlayer);
        if (btnClosePlayer != null) {
            btnClosePlayer.setOnClickListener(v -> dismiss());
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}