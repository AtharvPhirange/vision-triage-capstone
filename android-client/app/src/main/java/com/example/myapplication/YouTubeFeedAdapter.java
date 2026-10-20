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
import com.example.myapplication.data.model.YouTubeEyeVideo;

import java.util.ArrayList;
import java.util.List;

public class YouTubeFeedAdapter extends RecyclerView.Adapter<YouTubeFeedAdapter.ViewHolder> {

    private final Context context;
    private final List<YouTubeEyeVideo> items = new ArrayList<>();

    public YouTubeFeedAdapter(Context context) {
        this.context = context;
    }

    public void setVideos(List<YouTubeEyeVideo> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addVideos(List<YouTubeEyeVideo> moreItems) {
        if (moreItems != null && !moreItems.isEmpty()) {
            int start = items.size();
            items.addAll(moreItems);
            notifyItemRangeInserted(start, moreItems.size());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_youtube_feed_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        YouTubeEyeVideo video = items.get(position);

        if (holder.tvTitle != null) holder.tvTitle.setText(video.getTitle());
        if (holder.tvSubtitle != null) holder.tvSubtitle.setText(video.getChannelTitle() + " • " + video.getFormattedViews() + " • " + video.getPublishedTimeText());
        if (holder.tvDuration != null) holder.tvDuration.setText(video.getFormattedDuration());

        if (holder.imgThumbnail != null) Glide.with(context).load(video.getVideoThumbnail()).into(holder.imgThumbnail);
        if (holder.imgAvatar != null) Glide.with(context).load(video.getChannelThumbnail()).into(holder.imgAvatar);

        View.OnClickListener launchListener = v -> {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + video.getVideoId()));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + video.getVideoId()));
            try {
                context.startActivity(appIntent);
            } catch (Exception ex) {
                try {
                    context.startActivity(webIntent);
                } catch (Exception ignored) {}
            }
        };

        holder.itemView.setOnClickListener(launchListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail, imgAvatar;
        TextView tvTitle, tvSubtitle, tvDuration;

        ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgVideoThumbnail);
            imgAvatar = itemView.findViewById(R.id.imgChannelAvatar);
            tvTitle = itemView.findViewById(R.id.tvVideoTitle);
            tvSubtitle = itemView.findViewById(R.id.tvMetaSubtitle);
            tvDuration = itemView.findViewById(R.id.tvDurationPill);
        }
    }
}