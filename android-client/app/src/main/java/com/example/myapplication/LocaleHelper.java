package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import java.util.Locale;

public class LocaleHelper {

    private static final String PREF_NAME = "vision_triage_locale_prefs";
    public static final String KEY_LANGUAGE = "selected_language";
    public static final String KEY_IS_LANGUAGE_SET = "is_language_set";

    public static final String LANG_ENGLISH = "en";
    public static final String LANG_HINDI = "hi";
    public static final String LANG_MARATHI = "mr";

    public static Context onAttach(Context context) {
        String lang = getPersistedLanguage(context, LANG_ENGLISH);
        return updateResources(context, lang);
    }

    public static void setLocale(Context context, String languageCode) {
        if (context == null || languageCode == null || languageCode.trim().isEmpty()) return;

        persistLanguage(context, languageCode.trim());

        // Android Jetpack AppCompatDelegate setApplicationLocales for conflict-free locale switching
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageCode.trim());
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

    public static String getLanguage(Context context) {
        return getPersistedLanguage(context, LANG_ENGLISH);
    }

    public static String getSelectedLanguage(Context context) {
        return getPersistedLanguage(context, LANG_ENGLISH);
    }

    public static boolean isLanguageSelected(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LANGUAGE_SET, false);
    }

    private static String getPersistedLanguage(Context context, String defaultLanguage) {
        if (context == null) return defaultLanguage;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, defaultLanguage);
    }

    private static void persistLanguage(Context context, String languageCode) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_LANGUAGE, languageCode)
                .putBoolean(KEY_IS_LANGUAGE_SET, true)
                .apply();
    }

    public static Context updateResources(Context context, String languageCode) {
        if (context == null) return null;
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            return context.createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }
}