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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class FitnessFragment extends Fragment {

    public static class FitnessItem {
        String videoId;
        String title;
        String desc;
        String duration;
        String category;

        FitnessItem(String videoId, String title, String desc, String duration, String category) {
            this.videoId = videoId;
            this.title = title;
            this.desc = desc;
            this.duration = duration;
            this.category = category;
        }
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

        List<FitnessItem> items = new ArrayList<>();
        items.add(new FitnessItem("11gKlyS4mTo", "20-20-20 Ocular Reset Rule", "Rests ciliary muscles and prevents accommodation fatigue after screen exposure.", "3 Mins", "EYE STRAIN"));
        items.add(new FitnessItem("480_cKxIeOo", "Convergence & Focus Accommodation", "Pen-to-nose tracking exercise to improve binocular alignment.", "5 Mins", "ACCOMMODATION"));
        items.add(new FitnessItem("wX0k4oP7QhA", "Palming & Ocular Circulation", "Gentle darkness recovery to soothe dry, overworked eyes.", "4 Mins", "OCULAR CIRCULATION"));
        items.add(new FitnessItem("9XgNfH4P_8Y", "Blink Rate & Dry Eye Prevention", "Promotes uniform tear film distribution and prevents corneal dry spots.", "2 Mins", "DRY EYE"));
        items.add(new FitnessItem("480_cKxIeOo", "Pencil Push-Ups Binocular Tracking", "Strengthens extraocular muscles and improves vergence facility.", "4 Mins", "BINOCULAR"));
        items.add(new FitnessItem("11gKlyS4mTo", "Smooth Pursuit Figure Eight Movement", "Enhances saccadic tracking precision and relaxes eye strains.", "3 Mins", "MOTILITY"));

        if (rvFitnessCards != null) {
            rvFitnessCards.setAdapter(new FitnessAdapter(items, item -> openYouTubeVideo(requireContext(), item.videoId)));
        }
    }

    public static void openYouTubeVideo(Context context, String videoId) {
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
        private final List<FitnessItem> items;
        private final OnFitnessClickListener listener;

        interface OnFitnessClickListener {
            void onFitnessClick(FitnessItem item);
        }

        FitnessAdapter(List<FitnessItem> items, OnFitnessClickListener listener) {
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
            FitnessItem item = items.get(position);
            if (item != null) {
                if (holder.tvVideoTitle != null) holder.tvVideoTitle.setText(item.title);
                if (holder.tvVideoDescription != null) holder.tvVideoDescription.setText(item.desc);
                if (holder.tvDurationTag != null) holder.tvDurationTag.setText(item.duration);
                if (holder.tvCategoryPill != null) holder.tvCategoryPill.setText(item.category);

                String thumbnailUrl = "https://img.youtube.com/vi/" + item.videoId + "/hqdefault.jpg";
                if (holder.ivVideoThumbnail != null) {
                    Glide.with(holder.itemView.getContext())
                            .load(thumbnailUrl)
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

                if (holder.btnWatchVideo != null) {
                    holder.btnWatchVideo.setOnClickListener(v -> {
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
            TextView tvVideoTitle, tvVideoDescription, tvDurationTag, tvCategoryPill;
            MaterialButton btnWatchVideo;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                frameThumbnail = itemView.findViewById(R.id.frameThumbnail);
                ivVideoThumbnail = itemView.findViewById(R.id.ivVideoThumbnail);
                tvVideoTitle = itemView.findViewById(R.id.tvVideoTitle);
                tvVideoDescription = itemView.findViewById(R.id.tvVideoDescription);
                tvDurationTag = itemView.findViewById(R.id.tvDurationTag);
                tvCategoryPill = itemView.findViewById(R.id.tvCategoryPill);
                btnWatchVideo = itemView.findViewById(R.id.btnWatchVideo);
            }
        }
    }
}