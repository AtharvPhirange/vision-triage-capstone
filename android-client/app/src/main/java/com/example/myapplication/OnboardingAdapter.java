package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    public static final int REAL_SLIDE_COUNT = 3;

    private final int[] images = {
            R.drawable.ic_onboarding_pupil,   // Slide 1: Digital Pupillometer
            R.drawable.ic_onboarding_history,  // Slide 2: Precision Diagnostics
            R.drawable.ic_onboarding_secure    // Slide 3: Clinical Collaboration
    };

    private final int[] titlesRes = {
            R.string.onboarding_slide1_title,
            R.string.onboarding_slide2_title,
            R.string.onboarding_slide3_title
    };

    private final int[] descriptionsRes = {
            R.string.onboarding_slide1_desc,
            R.string.onboarding_slide2_desc,
            R.string.onboarding_slide3_desc
    };

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        int actualIndex = position % REAL_SLIDE_COUNT;
        holder.imageSlide.setImageResource(images[actualIndex]);
        holder.tvSlideTitle.setText(titlesRes[actualIndex]);
        holder.tvSlideDescription.setText(descriptionsRes[actualIndex]);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
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