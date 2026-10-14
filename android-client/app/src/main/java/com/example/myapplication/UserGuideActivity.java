package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class UserGuideActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        String currentLang = LocaleHelper.getSelectedLanguage(newBase);
        LocaleHelper.setLocale(newBase, currentLang);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enforce active Jetpack locale configuration before inflating layout
        String currentLang = LocaleHelper.getSelectedLanguage(this);
        LocaleHelper.setLocale(this, currentLang);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        MaterialButton btnGotIt = findViewById(R.id.btnGotIt);

        View cardStep1 = findViewById(R.id.cardStep1);
        View cardStep2 = findViewById(R.id.cardStep2);
        View cardStep3 = findViewById(R.id.cardStep3);
        View cardStep4 = findViewById(R.id.cardStep4);
        View cardStep5 = findViewById(R.id.cardStep5);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnGotIt != null) {
            btnGotIt.setOnClickListener(v -> finish());
        }

        // Bind Guide Step Cards to ViewPager2 Slider Index
        if (cardStep1 != null) {
            cardStep1.setOnClickListener(v -> openGuideSliderModal(0));
        }

        if (cardStep2 != null) {
            cardStep2.setOnClickListener(v -> openGuideSliderModal(1));
        }

        if (cardStep3 != null) {
            cardStep3.setOnClickListener(v -> openGuideSliderModal(2));
        }

        if (cardStep4 != null) {
            cardStep4.setOnClickListener(v -> openGuideSliderModal(3));
        }

        if (cardStep5 != null) {
            cardStep5.setOnClickListener(v -> openGuideSliderModal(4));
        }

        // Start Continuous 3D Floating Bubble Animations
        startFloatingAnimations();
    }

    private void startFloatingAnimations() {
        View cardStep1 = findViewById(R.id.cardStep1);
        View cardStep2 = findViewById(R.id.cardStep2);
        View cardStep3 = findViewById(R.id.cardStep3);
        View cardStep4 = findViewById(R.id.cardStep4);
        View cardStep5 = findViewById(R.id.cardStep5);

        float pxTranslation = 8f * getResources().getDisplayMetrics().density;

        animateCard(cardStep1, pxTranslation, 2800, 0);
        animateCard(cardStep2, -pxTranslation, 3200, 200);
        animateCard(cardStep3, pxTranslation, 3000, 400);
        animateCard(cardStep4, -pxTranslation, 3400, 600);
        animateCard(cardStep5, pxTranslation, 2900, 800);
    }

    private void animateCard(View view, float translationY, long duration, long startDelay) {
        if (view == null) return;
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0f, translationY);
        animator.setDuration(duration);
        animator.setStartDelay(startDelay);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    private void openGuideSliderModal(int initialIndex) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_fullscreen_guide_slider, null);
        bottomSheetDialog.setContentView(dialogView);

        // Configure Immediate Fullscreen Expansion (~95% Screen Height Expanded) & Blur Flag
        bottomSheetDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                behavior.setPeekHeight(screenHeight);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setFitToContents(false);
                behavior.setExpandedOffset((int) (screenHeight * 0.05));

                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = (int) (screenHeight * 0.95);
                bottomSheet.setLayoutParams(layoutParams);
            }
        });

        // Enable Window Dimming (0.65f) & Android 12+ Blur Flag
        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setDimAmount(0.65f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            }
        }

        TextView tvStepCounter = dialogView.findViewById(R.id.tvStepCounter);
        View btnClose = dialogView.findViewById(R.id.btnClose);
        ViewPager2 viewPagerGuide = dialogView.findViewById(R.id.viewPagerGuide);

        MaterialButton btnPrevSlide = dialogView.findViewById(R.id.btnPrevSlide);
        MaterialButton btnNextSlide = dialogView.findViewById(R.id.btnNextSlide);

        View[] dots = new View[]{
                dialogView.findViewById(R.id.dot0),
                dialogView.findViewById(R.id.dot1),
                dialogView.findViewById(R.id.dot2),
                dialogView.findViewById(R.id.dot3),
                dialogView.findViewById(R.id.dot4)
        };

        // Construct 5 Guide Step Slides with Dynamic Localized String Resources
        List<GuideSlideAdapter.SlideData> slides = new ArrayList<>();
        slides.add(new GuideSlideAdapter.SlideData(
                getString(R.string.guide_step1_badge), "#0284C7",
                getString(R.string.guide_step1_title), getString(R.string.guide_step1_desc),
                getString(R.string.patient_portal_title), getString(R.string.patient_portal_hub_desc),
                getString(R.string.technician_portal_title), getString(R.string.technician_portal_hub_desc),
                getString(R.string.consultant_portal_title), getString(R.string.consultant_portal_hub_desc),
                getString(R.string.guide_step1_item4_title), getString(R.string.guide_step1_item4_desc)
        ));

        slides.add(new GuideSlideAdapter.SlideData(
                getString(R.string.guide_step2_badge), "#6366F1",
                getString(R.string.guide_step2_title), getString(R.string.guide_step2_desc),
                getString(R.string.guide_step2_item1_title), getString(R.string.guide_step2_item1_desc),
                getString(R.string.guide_step2_item2_title), getString(R.string.guide_step2_item2_desc),
                getString(R.string.guide_step2_item3_title), getString(R.string.guide_step2_item3_desc),
                getString(R.string.guide_step2_item4_title), getString(R.string.guide_step2_item4_desc)
        ));

        slides.add(new GuideSlideAdapter.SlideData(
                getString(R.string.guide_step3_badge), "#1E1B4B",
                getString(R.string.guide_step3_title), getString(R.string.guide_step3_desc),
                getString(R.string.guide_step3_item1_title), getString(R.string.guide_step3_item1_desc),
                getString(R.string.guide_step3_item2_title), getString(R.string.guide_step3_item2_desc),
                getString(R.string.guide_step3_item3_title), getString(R.string.guide_step3_item3_desc),
                getString(R.string.guide_step3_item4_title), getString(R.string.guide_step3_item4_desc)
        ));

        slides.add(new GuideSlideAdapter.SlideData(
                getString(R.string.guide_step4_badge), "#EA580C",
                getString(R.string.guide_step4_title), getString(R.string.guide_step4_desc),
                getString(R.string.guide_step4_item1_title), getString(R.string.guide_step4_item1_desc),
                getString(R.string.guide_step4_item2_title), getString(R.string.guide_step4_item2_desc),
                getString(R.string.guide_step4_item3_title), getString(R.string.guide_step4_item3_desc),
                getString(R.string.guide_step4_item4_title), getString(R.string.guide_step4_item4_desc)
        ));

        slides.add(new GuideSlideAdapter.SlideData(
                getString(R.string.guide_step5_badge), "#16A34A",
                getString(R.string.guide_step5_title), getString(R.string.guide_step5_desc),
                getString(R.string.guide_step5_item1_title), getString(R.string.guide_step5_item1_desc),
                getString(R.string.guide_step5_item2_title), getString(R.string.guide_step5_item2_desc),
                getString(R.string.guide_step5_item3_title), getString(R.string.guide_step5_item3_desc),
                getString(R.string.guide_step5_item4_title), getString(R.string.guide_step5_item4_desc)
        ));

        if (viewPagerGuide != null) {
            GuideSlideAdapter adapter = new GuideSlideAdapter(slides);
            viewPagerGuide.setAdapter(adapter);
            viewPagerGuide.setCurrentItem(initialIndex, false);

            // Update UI State Function
            Runnable updatePageUI = () -> {
                int position = viewPagerGuide.getCurrentItem();
                if (tvStepCounter != null) {
                    tvStepCounter.setText(getString(R.string.step_counter_format, position + 1, 5));
                }

                if (btnPrevSlide != null) {
                    btnPrevSlide.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
                    btnPrevSlide.setText(getString(R.string.btn_prev));
                }

                if (btnNextSlide != null) {
                    btnNextSlide.setText(position == 4 ? getString(R.string.btn_finish_guide) : getString(R.string.btn_next_slide));
                }

                // Update Indicator Dots
                for (int i = 0; i < dots.length; i++) {
                    if (dots[i] != null) {
                        ViewGroup.LayoutParams params = dots[i].getLayoutParams();
                        if (i == position) {
                            params.width = dpToPx(16);
                            dots[i].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#003893")));
                        } else {
                            params.width = dpToPx(8);
                            dots[i].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
                        }
                        dots[i].setLayoutParams(params);
                    }
                }
            };

            // Trigger initial UI update
            updatePageUI.run();

            viewPagerGuide.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updatePageUI.run();
                }
            });

            if (btnPrevSlide != null) {
                btnPrevSlide.setOnClickListener(v -> {
                    int current = viewPagerGuide.getCurrentItem();
                    if (current > 0) {
                        viewPagerGuide.setCurrentItem(current - 1, true);
                    }
                });
            }

            if (btnNextSlide != null) {
                btnNextSlide.setOnClickListener(v -> {
                    int current = viewPagerGuide.getCurrentItem();
                    if (current < 4) {
                        viewPagerGuide.setCurrentItem(current + 1, true);
                    } else {
                        bottomSheetDialog.dismiss();
                    }
                });
            }
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}