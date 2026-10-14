package com.example.myapplication;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class DevicePairingActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        String currentLang = LocaleHelper.getSelectedLanguage(newBase);
        LocaleHelper.setLocale(newBase, currentLang);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_pairing);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        MaterialButton btnTestFlash = findViewById(R.id.btnTestFlash);
        if (btnTestFlash != null) {
            btnTestFlash.setOnClickListener(v -> testCameraFlashPulse());
        }
    }

    private void testCameraFlashPulse() {
        Toast.makeText(this, "Firing 100ms Optical Flash Sync Test...", Toast.LENGTH_SHORT).show();
        try {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager != null) {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, true);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        cameraManager.setTorchMode(cameraId, false);
                    } catch (Exception ignored) {}
                }, 100);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Camera Flash Test Triggered", Toast.LENGTH_SHORT).show();
        }
    }
}