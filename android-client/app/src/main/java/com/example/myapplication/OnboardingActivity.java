package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout layoutDots;
    private OnboardingAdapter adapter;
    private Button btnNext;
    private TextView btnSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        viewPager = findViewById(R.id.viewPager);
        layoutDots = findViewById(R.id.layoutDots);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);

        setupIndicators(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setupIndicators(position);
                if (position == adapter.getItemCount() - 1) {
                    btnNext.setText("Done");
                } else {
                    btnNext.setText("Next");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < adapter.getItemCount()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                navigateToHome();
            }
        });

        btnSkip.setOnClickListener(v -> navigateToHome());
    }

    private void setupIndicators(int position) {
        layoutDots.removeAllViews();
        int count = adapter.getItemCount();

        // Convert dp measurements to exact screen pixels dynamically using the multiplication operator (*)
        float density = getResources().getDisplayMetrics().density;
        int activeWidth = (int) (20 * density);
        int inactiveWidth = (int) (6 * density);
        int height = (int) (6 * density);
        int margin = (int) (6 * density);

        for (int i = 0; i < count; i++) {
            View indicator = new View(this);

            // If it is the current page, stretch the width to make the pill shape. Otherwise, keep it a dot.
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    i == position ? activeWidth : inactiveWidth, height);

            // Add spacing between indicators (except for the very last one)
            if (i != count - 1) {
                params.setMargins(0, 0, margin, 0);
            }

            indicator.setLayoutParams(params);

            // Apply the exact XML drawable shapes we created earlier
            indicator.setBackgroundResource(
                    i == position ? R.drawable.bg_indicator_active : R.drawable.bg_indicator_inactive
            );

            layoutDots.addView(indicator);
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(OnboardingActivity.this, AuthSignInActivity.class);
        startActivity(intent);
        finish();
    }
}