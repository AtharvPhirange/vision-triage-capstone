package com.example.myapplication;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        try {
            super.attachBaseContext(LocaleHelper.onAttach(newBase));
        } catch (Exception e) {
            super.attachBaseContext(newBase);
        }
    }
}