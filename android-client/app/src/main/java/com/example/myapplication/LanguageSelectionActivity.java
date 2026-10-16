package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

public class LanguageSelectionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        RadioGroup rgLanguages = findViewById(R.id.rgLanguages);
        MaterialButton btnContinueLang = findViewById(R.id.btnContinueLang);

        if (btnContinueLang != null) {
            btnContinueLang.setOnClickListener(v -> {
                String selectedCode = "en";

                if (rgLanguages != null) {
                    int checkedId = rgLanguages.getCheckedRadioButtonId();
                    if (checkedId == R.id.rbHindi) {
                        selectedCode = "hi";
                    } else if (checkedId == R.id.rbMarathi) {
                        selectedCode = "mr";
                    } else if (checkedId == R.id.rbSpanish) {
                        selectedCode = "es";
                    } else if (checkedId == R.id.rbFrench) {
                        selectedCode = "fr";
                    } else {
                        selectedCode = "en";
                    }
                }

                // Persist language configuration flag
                SharedPreferences localePrefs = getSharedPreferences("vision_triage_locale_prefs", MODE_PRIVATE);
                localePrefs.edit()
                        .putBoolean("is_language_set", true)
                        .putString("selected_language", selectedCode)
                        .apply();

                // Apply locale
                LocaleHelper.setLocale(LanguageSelectionActivity.this, selectedCode);

                Toast.makeText(this, "Language Selected", Toast.LENGTH_SHORT).show();

                // Proceed to Onboarding Intro Carousel
                Intent intent = new Intent(LanguageSelectionActivity.this, OnboardingActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}