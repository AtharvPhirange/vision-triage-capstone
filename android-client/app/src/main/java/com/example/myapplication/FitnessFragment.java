package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class FitnessFragment extends Fragment {

    public static class EyeFitnessItem {
        private final String videoId;
        private final @StringRes int titleResId;
        private final @StringRes int descResId;
        private final String channelName;
        private final String viewCount;
        private final String duration;
        private final String tag;
        private final String thumbnailUrl;

        public EyeFitnessItem(String videoId, @StringRes int titleResId, @StringRes int descResId, String channelName, String viewCount, String duration, String tag) {
            this.videoId = videoId;
            this.titleResId = titleResId;
            this.descResId = descResId;
            this.channelName = channelName;
            this.viewCount = viewCount;
            this.duration = duration;
            this.tag = tag;
            this.thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
        }

        public String getVideoId() { return videoId; }
        public int getTitleResId() { return titleResId; }
        public int getDescResId() { return descResId; }
        public String getChannelName() { return channelName; }
        public String getViewCount() { return viewCount; }
        public String getDuration() { return duration; }
        public String getTag() { return tag; }
        public String getThumbnailUrl() { return thumbnailUrl; }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fitness, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvFitnessCards = view.findViewById(R.id.rvFitnessCards);
        if (rvFitnessCards != null) {
            rvFitnessCards.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        List<EyeFitnessItem> items = new ArrayList<>();
        items.add(new EyeFitnessItem("_LBb9DMqCFc", R.string.fitness_title_2, R.string.fitness_desc_2, "By BRIGHT SIDE", "👁 27M views", "11 Mins", "MOBILITY & FOCUS"));
        items.add(new EyeFitnessItem("0jf9YPGKlbg", R.string.fitness_title_1, R.string.fitness_desc_1, "By Dr. Alan Mandell", "👁 2.8M views", "5 Mins", "DIGITAL STRAIN"));
        items.add(new EyeFitnessItem("YTvgqIGffW4", R.string.fitness_title_3, R.string.fitness_desc_3, "By ZEISS Vision Care", "👁 1.9M views", "3 Mins", "EYE STRAIN"));
        items.add(new EyeFitnessItem("5c2PZl84qK4", R.string.fitness_title_4, R.string.fitness_desc_4, "By Natural Vision", "👁 1.2M views", "6 Mins", "ACCOMMODATION"));
        items.add(new EyeFitnessItem("RoIYAoAalmI", R.string.fitness_title_5, R.string.fitness_desc_5, "By UC Health", "👁 950K views", "2 Mins", "YOGA & RELAX"));

        if (rvFitnessCards != null) {
            rvFitnessCards.setAdapter(new FitnessAdapter(items, item -> watchYoutubeExternal(requireContext(), item.getVideoId())));
        }
    }

    private static void watchYoutubeExternal(Context context, String videoId) {
        if (context == null || videoId == null || videoId.trim().isEmpty()) return;
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            try {
                context.startActivity(webIntent);
            } catch (Exception ignored) {}
        }
    }

    // --- Inner Fitness Adapter ---
    private static class FitnessAdapter extends RecyclerView.Adapter<FitnessAdapter.ViewHolder> {
        private final List<EyeFitnessItem> items;
        private final OnFitnessClickListener listener;

        interface OnFitnessClickListener {
            void onFitnessClick(EyeFitnessItem item);
        }

        FitnessAdapter(List<EyeFitnessItem> items, OnFitnessClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_eye_fitness_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EyeFitnessItem item = items.get(position);
            if (item != null) {
                Context strictContext = holder.itemView.getContext();
                if (holder.tvVideoTitle != null) holder.tvVideoTitle.setText(strictContext.getString(item.getTitleResId()));
                if (holder.tvChannelName != null) holder.tvChannelName.setText(item.getChannelName());
                if (holder.tvVideoDescription != null) holder.tvVideoDescription.setText(strictContext.getString(item.getDescResId()));
                if (holder.tvViewCount != null) holder.tvViewCount.setText(item.getViewCount());
                if (holder.tvDurationTag != null) holder.tvDurationTag.setText(item.getDuration());
                if (holder.tvCategoryPill != null) holder.tvCategoryPill.setText(item.getTag());

                if (holder.ivVideoThumbnail != null) {
                    Glide.with(strictContext)
                            .load(item.getThumbnailUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.bg_thumbnail_placeholder)
                            .error(R.drawable.bg_thumbnail_fallback)
                            .centerCrop()
                            .into(holder.ivVideoThumbnail);

                    holder.ivVideoThumbnail.setOnClickListener(v -> {
                        if (listener != null) listener.onFitnessClick(item);
                    });
                }

                if (holder.frameThumbnail != null) {
                    holder.frameThumbnail.setOnClickListener(v -> {
                        if (listener != null) listener.onFitnessClick(item);
                    });
                }

                if (holder.btnWatchYouTube != null) {
                    holder.btnWatchYouTube.setOnClickListener(v -> {
                        if (listener != null) listener.onFitnessClick(item);
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View frameThumbnail;
            ImageView ivVideoThumbnail;
            TextView tvVideoTitle, tvChannelName, tvVideoDescription, tvViewCount, tvDurationTag, tvCategoryPill;
            MaterialButton btnWatchYouTube;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                frameThumbnail = itemView.findViewById(R.id.frameThumbnail);
                ivVideoThumbnail = itemView.findViewById(R.id.ivVideoThumbnail);
                tvVideoTitle = itemView.findViewById(R.id.tvVideoTitle);
                tvChannelName = itemView.findViewById(R.id.tvChannelName);
                tvVideoDescription = itemView.findViewById(R.id.tvVideoDescription);
                tvViewCount = itemView.findViewById(R.id.tvViewCount);
                tvDurationTag = itemView.findViewById(R.id.tvDurationTag);
                tvCategoryPill = itemView.findViewById(R.id.tvCategoryPill);
                btnWatchYouTube = itemView.findViewById(R.id.btnWatchYouTube);
            }
        }
    }
}