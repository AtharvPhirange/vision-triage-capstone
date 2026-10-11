package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    // Updated with exact file names from your drawable folder
    private final int[] images = {
            R.drawable.ic_onboarding_pupil,   // Slide 1 (Digital Pupillometer)
            R.drawable.ic_onboarding_history,  // Slide 2 (Health History)
            R.drawable.ic_onboarding_secure    // Slide 3 (Secure & Local Data)
    };

    // Aligned with your app titles and descriptions
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

    public static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageSlide;
        TextView tvSlideTitle;
        TextView tvSlideDescription;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageSlide = itemView.findViewById(R.id.imgIllustration);
            tvSlideTitle = itemView.findViewById(R.id.tvTitle);
            tvSlideDescription = itemView.findViewById(R.id.tvSubtitle);
        }
    }
}