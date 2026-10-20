package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.myapplication.data.model.YouTubeEyeVideo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class YouTubeFeedAdapter extends RecyclerView.Adapter<YouTubeFeedAdapter.ViewHolder> {

    private final Context context;
    private final List<YouTubeEyeVideo> items = new ArrayList<>();
    private final Set<String> itemIds = new HashSet<>();

    public YouTubeFeedAdapter(Context context) {
        this.context = context;
    }

    public void setVideos(List<YouTubeEyeVideo> newItems) {
        items.clear();
        itemIds.clear();
        if (newItems != null) {
            for (YouTubeEyeVideo v : newItems) {
                if (itemIds.add(v.getVideoId())) {
                    items.add(v);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void addVideos(List<YouTubeEyeVideo> moreItems) {
        if (moreItems == null || moreItems.isEmpty()) return;
        int startPos = items.size();
        int added = 0;
        for (YouTubeEyeVideo v : moreItems) {
            if (itemIds.add(v.getVideoId())) {
                items.add(v);
                added++;
            }
        }
        if (added > 0) notifyItemRangeInserted(startPos, added);
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

        // Load Thumbnail with diagnostics
        if (holder.imgThumbnail != null) {
            Glide.with(context)
                    .load(video.getVideoThumbnail())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("GlideThumb", "Thumbnail load failed for URL: " + model, e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.imgThumbnail);
        }

        if (holder.imgAvatar != null) {
            Glide.with(context)
                    .load(video.getChannelThumbnail())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgAvatar);
        }

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