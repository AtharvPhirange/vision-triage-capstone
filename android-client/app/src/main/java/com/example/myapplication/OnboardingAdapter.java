package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    // Using built-in Android icons as placeholders until you add your own vectors
    private final int[] images = {
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_recent_history,
            android.R.drawable.ic_menu_manage
    };

    private final String[] titles = {
            "Digital Pupillometer",
            "Access Your Health History",
            "Secure & Local Data"
    };

    private final String[] descriptions = {
            "Analyze pupillary light reflexes instantly using advanced mobile computer vision.",
            "All your diagnostic scans and triage results are available in seconds.",
            "Your health data never leaves your device without your explicit consent."
    };

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.imageSlide.setImageResource(images[position]);
        holder.tvSlideTitle.setText(titles[position]);
        holder.tvSlideDescription.setText(descriptions[position]);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageSlide;
        TextView tvSlideTitle;
        TextView tvSlideDescription;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageSlide = itemView.findViewById(R.id.imageSlide);
            tvSlideTitle = itemView.findViewById(R.id.tvSlideTitle);
            tvSlideDescription = itemView.findViewById(R.id.tvSlideDescription);
        }
    }
}