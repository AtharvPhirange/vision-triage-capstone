package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class EyeExerciseAdapter extends RecyclerView.Adapter<EyeExerciseAdapter.VideoViewHolder> {

    private final Context context;
    private final List<EyeExerciseVideo> videos = new ArrayList<>();

    public EyeExerciseAdapter(Context context) {
        this.context = context;
    }

    public void setVideos(List<EyeExerciseVideo> newVideos) {
        this.videos.clear();
        if (newVideos != null) {
            this.videos.addAll(newVideos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_eye_video_card, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        EyeExerciseVideo item = videos.get(position);

        if (holder.tvTitle != null) holder.tvTitle.setText(item.getTitle());
        if (holder.tvChannel != null) holder.tvChannel.setText(item.getChannelTitle());
        if (holder.tvDescription != null) holder.tvDescription.setText(item.getDescription());
        if (holder.tvViewBadge != null) holder.tvViewBadge.setText("👁 " + item.getFormattedViewCount());
        if (holder.tvDuration != null) holder.tvDuration.setText(item.getDuration());
        if (holder.tvCategoryTag != null) holder.tvCategoryTag.setText(item.getCategoryTag());

        // Load high-res thumbnail with Glide
        if (holder.imgThumbnail != null) {
            Glide.with(context)
                    .load(item.getThumbnailUrl())
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.imgThumbnail);
        }

        // Click actions: Open directly in YouTube App or fallback to Browser
        View.OnClickListener launchYouTube = v -> {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + item.getVideoId()));
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + item.getVideoId()));
            try {
                context.startActivity(appIntent);
            } catch (Exception ex) {
                try {
                    context.startActivity(webIntent);
                } catch (Exception ignored) {}
            }
        };

        if (holder.layoutThumbnail != null) holder.layoutThumbnail.setOnClickListener(launchYouTube);
        if (holder.btnWatch != null) holder.btnWatch.setOnClickListener(launchYouTube);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView tvViewBadge, tvDuration, tvTitle, tvChannel, tvDescription, tvCategoryTag;
        MaterialButton btnWatch;
        View layoutThumbnail;

        VideoViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            tvViewBadge = itemView.findViewById(R.id.tvViewBadge);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvTitle = itemView.findViewById(R.id.tvVideoTitle);
            tvChannel = itemView.findViewById(R.id.tvChannelTitle);
            tvDescription = itemView.findViewById(R.id.tvVideoDescription);
            tvCategoryTag = itemView.findViewById(R.id.tvCategoryTag);
            btnWatch = itemView.findViewById(R.id.btnWatchYouTube);
            layoutThumbnail = itemView.findViewById(R.id.layoutThumbnail);
        }
    }
}