package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class EyeFitnessActivity extends AppCompatActivity {

    private static class FitnessItem {
        String title;
        String desc;
        String duration;
        String videoUrl;

        FitnessItem(String title, String desc, String duration, String videoUrl) {
            this.title = title;
            this.desc = desc;
            this.duration = duration;
            this.videoUrl = videoUrl;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String currentLang = LocaleHelper.getSelectedLanguage(newBase);
        LocaleHelper.setLocale(newBase, currentLang);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eye_fitness);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        RecyclerView rvFitnessCards = findViewById(R.id.rvFitnessCards);
        if (rvFitnessCards != null) {
            rvFitnessCards.setLayoutManager(new LinearLayoutManager(this));
        }

        List<FitnessItem> items = new ArrayList<>();
        items.add(new FitnessItem("20-20-20 Ocular Reset Rule", "Rests ciliary muscles and prevents accommodation fatigue after screen exposure.", "3 Mins", "https://www.youtube.com/watch?v=11gKlyS4mTo"));
        items.add(new FitnessItem("Convergence & Focus Accommodation", "Pen-to-nose tracking exercise to improve binocular alignment.", "5 Mins", "https://www.youtube.com/watch?v=480_cKxIeOo"));
        items.add(new FitnessItem("Palming & Ocular Circulation", "Gentle darkness recovery to soothe dry, overworked eyes.", "4 Mins", "https://www.youtube.com/watch?v=wX0k4oP7QhA"));
        items.add(new FitnessItem("Blink Rate & Dry Eye Prevention", "Promotes uniform tear film distribution and prevents corneal dry spots.", "2 Mins", "https://www.youtube.com/watch?v=9XgNfH4P_8Y"));

        if (rvFitnessCards != null) {
            rvFitnessCards.setAdapter(new FitnessAdapter(items, item -> {
                Intent videoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.videoUrl));
                try {
                    startActivity(videoIntent);
                } catch (Exception e) {
                    Toast.makeText(EyeFitnessActivity.this, "Opening Video in Browser...", Toast.LENGTH_SHORT).show();
                }
            }));
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fitness_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FitnessItem item = items.get(position);
            if (item != null) {
                if (holder.tvExerciseTitle != null) holder.tvExerciseTitle.setText(item.title);
                if (holder.tvExerciseDesc != null) holder.tvExerciseDesc.setText(item.desc);
                if (holder.tvDurationBadge != null) holder.tvDurationBadge.setText(item.duration);

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
            TextView tvExerciseTitle, tvExerciseDesc, tvDurationBadge;
            MaterialButton btnWatchVideo;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvExerciseTitle = itemView.findViewById(R.id.tvExerciseTitle);
                tvExerciseDesc = itemView.findViewById(R.id.tvExerciseDesc);
                tvDurationBadge = itemView.findViewById(R.id.tvDurationBadge);
                btnWatchVideo = itemView.findViewById(R.id.btnWatchVideo);
            }
        }
    }
}