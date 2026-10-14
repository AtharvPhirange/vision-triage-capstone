package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class LocaleHelper {

    private static final String PREF_NAME = "vision_triage_locale_prefs";
    public static final String KEY_LANGUAGE = "selected_language";
    public static final String KEY_IS_LANGUAGE_SET = "is_language_set";

    public static void setLocale(Context context, String langCode) {
        if (context == null || langCode == null || langCode.trim().isEmpty()) return;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_LANGUAGE, langCode.trim())
                .putBoolean(KEY_IS_LANGUAGE_SET, true)
                .apply();

        // Android Jetpack AppCompatDelegate setApplicationLocales for conflict-free locale switching
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(langCode.trim());
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    public static String getSelectedLanguage(Context context) {
        if (context == null) return "en";
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    public static boolean isLanguageSelected(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LANGUAGE_SET, false);
    }
}