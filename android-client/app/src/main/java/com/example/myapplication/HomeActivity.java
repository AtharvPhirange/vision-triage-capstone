package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class HomeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageView imgNavHome, imgNavDiscover, imgNavFitness, imgNavGuide, imgNavMyHealth;
    private TextView tvNavHome, tvNavDiscover, tvNavFitness, tvNavGuide, tvNavMyHealth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView btnSettings = findViewById(R.id.btnSettings);
        ImageView btnLanguage = findViewById(R.id.btnLanguage);
        View btnQuickScanner = findViewById(R.id.btnQuickScanner);
        ImageView btnUserProfile = findViewById(R.id.btnUserProfile);

        // Load custom photo into top bar avatar if present
        String photoUri = SessionManager.getUserPhotoUri(this);
        if (btnUserProfile != null && photoUri != null) {
            Glide.with(this)
                    .load(Uri.parse(photoUri))
                    .placeholder(R.drawable.ic_account_circle)
                    .error(R.drawable.ic_account_circle)
                    .circleCrop()
                    .into(btnUserProfile);
        }

        // ViewPager2 Initialization
        viewPager = findViewById(R.id.dashboardViewPager);
        if (viewPager != null) {
            DashboardPagerAdapter pagerAdapter = new DashboardPagerAdapter(this);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setOffscreenPageLimit(4); // Preload all 5 tabs for stutter-free swiping

            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updateTabUI(position);
                }
            });
        }

        // Bottom Nav Tab Views
        View tabNavHome = findViewById(R.id.tabNavHome);
        View tabNavDiscover = findViewById(R.id.tabNavDiscover);
        View tabNavFitness = findViewById(R.id.tabNavFitness);
        View tabNavGuide = findViewById(R.id.tabNavGuide);
        View tabNavMyHealth = findViewById(R.id.tabNavMyHealth);

        imgNavHome = findViewById(R.id.imgNavHome);
        imgNavDiscover = findViewById(R.id.imgNavDiscover);
        imgNavFitness = findViewById(R.id.imgNavFitness);
        imgNavGuide = findViewById(R.id.imgNavGuide);
        imgNavMyHealth = findViewById(R.id.imgNavMyHealth);

        tvNavHome = findViewById(R.id.tvNavHome);
        tvNavDiscover = findViewById(R.id.tvNavDiscover);
        tvNavFitness = findViewById(R.id.tvNavFitness);
        tvNavGuide = findViewById(R.id.tvNavGuide);
        tvNavMyHealth = findViewById(R.id.tvNavMyHealth);

        if (savedInstanceState == null) {
            updateTabUI(0);
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> showDashboardMenuModal());
        }

        if (btnUserProfile != null) {
            btnUserProfile.setOnClickListener(v -> {
                UserProfileBottomSheet sheet = new UserProfileBottomSheet();
                sheet.show(getSupportFragmentManager(), "UserProfileBottomSheet");
            });
        }

        if (btnLanguage != null) {
            btnLanguage.setOnClickListener(v -> showLanguageSelectionDialog());
        }

        if (btnQuickScanner != null) {
            btnQuickScanner.setContentDescription("Scan Peer Clinical QR Code");
            btnQuickScanner.setOnClickListener(v -> {
                Intent qrScannerIntent = new Intent(this, QrReportScannerActivity.class);
                startActivity(qrScannerIntent);
            });
        }

        // Floating Bottom Dock Tab Listeners -> Slide ViewPager2
        if (tabNavHome != null) {
            tabNavHome.setOnClickListener(v -> {
                if (viewPager != null) viewPager.setCurrentItem(0, true);
            });
        }

        if (tabNavDiscover != null) {
            tabNavDiscover.setOnClickListener(v -> {
                if (viewPager != null) viewPager.setCurrentItem(1, true);
            });
        }

        if (tabNavFitness != null) {
            tabNavFitness.setOnClickListener(v -> {
                if (viewPager != null) viewPager.setCurrentItem(2, true);
            });
        }

        if (tabNavGuide != null) {
            tabNavGuide.setOnClickListener(v -> {
                if (viewPager != null) viewPager.setCurrentItem(3, true);
            });
        }

        if (tabNavMyHealth != null) {
            tabNavMyHealth.setOnClickListener(v -> {
                if (viewPager != null) viewPager.setCurrentItem(4, true);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageView btnUserProfile = findViewById(R.id.btnUserProfile);
        String photoUri = SessionManager.getUserPhotoUri(this);
        if (btnUserProfile != null) {
            if (photoUri != null && !photoUri.isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(photoUri))
                        .placeholder(R.drawable.ic_account_circle)
                        .error(R.drawable.ic_account_circle)
                        .circleCrop()
                        .into(btnUserProfile);
            } else {
                btnUserProfile.setImageResource(R.drawable.ic_account_circle);
            }
        }
    }

    private void updateTabUI(int activeIndex) {
        int activeColor = Color.parseColor("#0284C7");
        int inactiveColor = Color.parseColor("#64748B");

        if (imgNavHome != null) imgNavHome.setColorFilter(activeIndex == 0 ? activeColor : inactiveColor);
        if (tvNavHome != null) {
            tvNavHome.setTextColor(activeIndex == 0 ? activeColor : inactiveColor);
            tvNavHome.setTypeface(null, activeIndex == 0 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }

        if (imgNavDiscover != null) imgNavDiscover.setColorFilter(activeIndex == 1 ? activeColor : inactiveColor);
        if (tvNavDiscover != null) {
            tvNavDiscover.setTextColor(activeIndex == 1 ? activeColor : inactiveColor);
            tvNavDiscover.setTypeface(null, activeIndex == 1 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }

        if (imgNavFitness != null) imgNavFitness.setColorFilter(activeIndex == 2 ? activeColor : inactiveColor);
        if (tvNavFitness != null) {
            tvNavFitness.setTextColor(activeIndex == 2 ? activeColor : inactiveColor);
            tvNavFitness.setTypeface(null, activeIndex == 2 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }

        if (imgNavGuide != null) imgNavGuide.setColorFilter(activeIndex == 3 ? activeColor : inactiveColor);
        if (tvNavGuide != null) {
            tvNavGuide.setTextColor(activeIndex == 3 ? activeColor : inactiveColor);
            tvNavGuide.setTypeface(null, activeIndex == 3 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }

        if (imgNavMyHealth != null) imgNavMyHealth.setColorFilter(activeIndex == 4 ? activeColor : inactiveColor);
        if (tvNavMyHealth != null) {
            tvNavMyHealth.setTextColor(activeIndex == 4 ? activeColor : inactiveColor);
            tvNavMyHealth.setTypeface(null, activeIndex == 4 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }

    private void showLanguageSelectionDialog() {
        String[] languages = {"English", "हिन्दी (Hindi)", "मराठी (Marathi)", "Español (Spanish)", "Français (French)"};
        String[] codes = {"en", "hi", "mr", "es", "fr"};
        String current = LocaleHelper.getSelectedLanguage(this);
        int selectedIndex = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equalsIgnoreCase(current)) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Language / भाषा चुनें")
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String selectedCode = codes[which];
                    LocaleHelper.setLocale(HomeActivity.this, selectedCode);
                    Toast.makeText(HomeActivity.this, "Language Selected: " + languages[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    // Hard activity restart to flush ViewPager2 and fragment string caches
                    Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDashboardMenuModal() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View menuView = getLayoutInflater().inflate(R.layout.layout_dashboard_menu_sheet, null);
        bottomSheetDialog.setContentView(menuView);

        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setDimAmount(0.65f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            }
        }

        View menuClinicalGuide = menuView.findViewById(R.id.menuClinicalGuide);
        View menuReportIssue = menuView.findViewById(R.id.menuReportIssue);
        View menuDevicePairing = menuView.findViewById(R.id.menuDevicePairing);

        View menuHelpline = menuView.findViewById(R.id.menuHelpline);
        View menuDataPrivacy = menuView.findViewById(R.id.menuDataPrivacy);
        View menuPreferences = menuView.findViewById(R.id.menuPreferences);
        View menuSignOut = menuView.findViewById(R.id.menuSignOut);

        if (menuClinicalGuide != null) {
            menuClinicalGuide.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(HomeActivity.this, UserGuideActivity.class));
            });
        }

        if (menuReportIssue != null) {
            menuReportIssue.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                new ReportIssueBottomSheet().show(getSupportFragmentManager(), "ReportIssue");
            });
        }

        if (menuDevicePairing != null) {
            menuDevicePairing.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(HomeActivity.this, DevicePairingActivity.class));
            });
        }

        if (menuHelpline != null) {
            menuHelpline.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                new HelpSupportBottomSheet().show(getSupportFragmentManager(), "HelpSupport");
            });
        }

        if (menuDataPrivacy != null) {
            menuDataPrivacy.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                new DataPrivacyBottomSheet().show(getSupportFragmentManager(), "DataPrivacy");
            });
        }

        if (menuPreferences != null) {
            menuPreferences.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(HomeActivity.this, AppPreferencesActivity.class));
            });
        }

        if (menuSignOut != null) {
            menuSignOut.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                SessionManager.clearSessionAndLogout(HomeActivity.this);
                Toast.makeText(this, "Signed Out Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        bottomSheetDialog.show();
    }
}