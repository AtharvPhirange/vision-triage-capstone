package com.example.myapplication;

import android.content.Context;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        try {
            String langCode = LocaleHelper.getSelectedLanguage(newBase);
            Locale locale = new Locale(langCode);
            Locale.setDefault(locale);

            Configuration config = new Configuration(newBase.getResources().getConfiguration());
            config.setLocale(locale);
            Context context = newBase.createConfigurationContext(config);
            super.attachBaseContext(context);
        } catch (Exception e) {
            super.attachBaseContext(newBase);
        }
    }
}