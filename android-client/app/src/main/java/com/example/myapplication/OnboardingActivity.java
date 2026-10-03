package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout layoutDots;
    private OnboardingAdapter adapter;
    private TextView[] dots;
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
        dots = new TextView[adapter.getItemCount()];
        layoutDots.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;")); // Bullet character
            dots[i].setTextSize(35);
            dots[i].setTextColor(ContextCompat.getColor(this,
                    i == position ? R.color.clinical_blue : R.color.card_stroke));
            layoutDots.addView(dots[i]);
        }
    }

    private void navigateToHome() {
        // Changed destination from HomeActivity to LoginActivity
        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}