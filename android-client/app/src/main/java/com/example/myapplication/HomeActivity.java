package com.example.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Random;

public class HomeActivity extends AppCompatActivity {

    private ImageView imgNavHome, imgNavDiscover, imgNavFitness, imgNavMyHealth;
    private TextView tvNavHome, tvNavDiscover, tvNavFitness, tvNavMyHealth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView btnSettings = findViewById(R.id.btnSettings);
        ImageView btnLanguage = findViewById(R.id.btnLanguage);
        ImageView btnNotification = findViewById(R.id.btnNotification);
        View btnUserProfile = findViewById(R.id.btnUserProfile);

        // Bottom Nav Tab Views
        View tabNavHome = findViewById(R.id.tabNavHome);
        View tabNavDiscover = findViewById(R.id.tabNavDiscover);
        View tabNavFitness = findViewById(R.id.tabNavFitness);
        View tabNavMyHealth = findViewById(R.id.tabNavMyHealth);

        imgNavHome = findViewById(R.id.imgNavHome);
        imgNavDiscover = findViewById(R.id.imgNavDiscover);
        imgNavFitness = findViewById(R.id.imgNavFitness);
        imgNavMyHealth = findViewById(R.id.imgNavMyHealth);

        tvNavHome = findViewById(R.id.tvNavHome);
        tvNavDiscover = findViewById(R.id.tvNavDiscover);
        tvNavFitness = findViewById(R.id.tvNavFitness);
        tvNavMyHealth = findViewById(R.id.tvNavMyHealth);

        if (savedInstanceState == null) {
            switchFragment(new HomeFragment(), "HOME");
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

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show());
        }

        // Floating Bottom Dock Tab Listeners
        if (tabNavHome != null) {
            tabNavHome.setOnClickListener(v -> {
                switchFragment(new HomeFragment(), "HOME");
                updateTabUI(0);
            });
        }

        if (tabNavDiscover != null) {
            tabNavDiscover.setOnClickListener(v -> {
                switchFragment(new DiscoverFragment(), "DISCOVER");
                updateTabUI(1);
            });
        }

        if (tabNavFitness != null) {
            tabNavFitness.setOnClickListener(v -> {
                switchFragment(new FitnessFragment(), "FITNESS");
                updateTabUI(2);
            });
        }

        if (tabNavMyHealth != null) {
            tabNavMyHealth.setOnClickListener(v -> showEyeHealthTipModal());
        }
    }

    private void switchFragment(Fragment targetFragment, String tag) {
        if (targetFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFragmentContainer, targetFragment, tag)
                    .setReorderingAllowed(true)
                    .commit();
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
    }

    private void showEyeHealthTipModal() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_eye_health_tip);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTipBody = dialog.findViewById(R.id.tvTipBody);
        MaterialButton btnShuffleTip = dialog.findViewById(R.id.btnShuffleTip);
        MaterialButton btnDismissTip = dialog.findViewById(R.id.btnDismissTip);

        String[] tips = getResources().getStringArray(R.array.clinical_eye_tips);
        Random random = new Random();

        if (tvTipBody != null && tips.length > 0) {
            tvTipBody.setText(tips[random.nextInt(tips.length)]);
        }

        if (btnShuffleTip != null && tvTipBody != null && tips.length > 0) {
            btnShuffleTip.setOnClickListener(v -> {
                int nextIndex = random.nextInt(tips.length);
                tvTipBody.setAlpha(0f);
                tvTipBody.setText(tips[nextIndex]);
                tvTipBody.animate().alpha(1f).setDuration(300).start();
            });
        }

        if (btnDismissTip != null) {
            btnDismissTip.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
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