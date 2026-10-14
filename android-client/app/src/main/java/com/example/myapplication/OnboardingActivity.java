package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout layoutDots;
    private OnboardingAdapter adapter;
    private Button btnNext;

    // Auto-Slide Handler & Runnable
    private final Handler autoSlideHandler = new Handler(Looper.getMainLooper());
    private Runnable autoSlideRunnable;
    private static final long AUTO_SLIDE_DELAY_MS = 3500; // 3.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Language Gateway Interception: Route to LanguageSelectionActivity if language is unconfigured
        SharedPreferences localePrefs = getSharedPreferences("vision_triage_locale_prefs", MODE_PRIVATE);
        boolean isLanguageSet = localePrefs.getBoolean("is_language_set", false);

        if (!isLanguageSet) {
            Intent intent = new Intent(this, LanguageSelectionActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Persistent Session Interception: Skip onboarding if user is already authenticated
        if (SessionManager.isLoggedIn(this)) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        // Hide default action bar for clean full-screen onboarding experience
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        viewPager = findViewById(R.id.viewPager);
        layoutDots = findViewById(R.id.layoutDots);
        btnNext = findViewById(R.id.btnNext);
        TextView btnSkip = findViewById(R.id.btnSkip);

        adapter = new OnboardingAdapter();
        if (viewPager != null) {
            viewPager.setAdapter(adapter);

            // Offscreen Page Limit for Stutter-Free Rendering
            viewPager.setOffscreenPageLimit(3);

            // Centered Initial Position for Bidirectional Swiping
            int middlePosition = (Integer.MAX_VALUE / 2) - ((Integer.MAX_VALUE / 2) % OnboardingAdapter.REAL_SLIDE_COUNT);
            viewPager.setCurrentItem(middlePosition, false);

            setupIndicators(middlePosition % OnboardingAdapter.REAL_SLIDE_COUNT);

            // Custom Page Transformers for Scale & Fade Carousel Polish
            CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
            float density = getResources().getDisplayMetrics().density;
            compositePageTransformer.addTransformer(new MarginPageTransformer((int) (16 * density)));
            compositePageTransformer.addTransformer((page, position) -> {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.92f + r * 0.08f);
                page.setAlpha(0.6f + r * 0.4f);
            });
            viewPager.setPageTransformer(compositePageTransformer);

            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    int actualIndex = position % OnboardingAdapter.REAL_SLIDE_COUNT;
                    setupIndicators(actualIndex);

                    // Dynamic button label transformation ("Next" vs "Done")
                    if (btnNext != null) {
                        if (actualIndex == OnboardingAdapter.REAL_SLIDE_COUNT - 1) {
                            btnNext.setText(R.string.btn_done);
                        } else {
                            btnNext.setText(R.string.btn_next);
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    super.onPageScrollStateChanged(state);
                    // Reset auto-slide timer when user manually swipes
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        startAutoSlide();
                    }
                }
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (viewPager != null) {
                    int current = viewPager.getCurrentItem();
                    int actualIndex = current % OnboardingAdapter.REAL_SLIDE_COUNT;
                    if (actualIndex == OnboardingAdapter.REAL_SLIDE_COUNT - 1) {
                        navigateToLogin();
                    } else {
                        viewPager.setCurrentItem(current + 1, true);
                        startAutoSlide();
                    }
                } else {
                    navigateToLogin();
                }
            });
        }

        if (btnSkip != null) {
            btnSkip.setOnClickListener(v -> navigateToLogin());
        }
    }

    private void startAutoSlide() {
        stopAutoSlide();
        autoSlideRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewPager != null) {
                    int current = viewPager.getCurrentItem();
                    viewPager.setCurrentItem(current + 1, true); // Monotonically advances forward
                }
                autoSlideHandler.postDelayed(this, AUTO_SLIDE_DELAY_MS);
            }
        };
        autoSlideHandler.postDelayed(autoSlideRunnable, AUTO_SLIDE_DELAY_MS);
    }

    private void stopAutoSlide() {
        if (autoSlideRunnable != null) {
            autoSlideHandler.removeCallbacks(autoSlideRunnable);
            autoSlideRunnable = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoSlide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoSlide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoSlide();
    }

    private void setupIndicators(int actualIndex) {
        if (layoutDots == null) return;

        layoutDots.removeAllViews();
        int count = OnboardingAdapter.REAL_SLIDE_COUNT;

        float density = getResources().getDisplayMetrics().density;
        int activeWidth = (int) (22 * density);
        int inactiveWidth = (int) (8 * density);
        int height = (int) (8 * density);
        int margin = (int) (6 * density);

        for (int i = 0; i < count; i++) {
            View indicator = new View(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    i == actualIndex ? activeWidth : inactiveWidth, height);

            if (i != count - 1) {
                params.setMargins(0, 0, margin, 0);
            }

            indicator.setLayoutParams(params);

            // Active dot uses primary brand blue background (#003893)
            indicator.setBackgroundResource(
                    i == actualIndex ? R.drawable.bg_indicator_active : R.drawable.bg_indicator_inactive
            );

            layoutDots.addView(indicator);
        }
    }

    private void navigateToLogin() {
        stopAutoSlide();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}