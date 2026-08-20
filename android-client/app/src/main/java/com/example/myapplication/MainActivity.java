package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ui.ScanHistoryActivity;

public class MainActivity extends AppCompatActivity {

    private OverlayView overlayView;
    private TextView tvScanStatus;
    private TextView tvTimer;
    private Button btnStartScan;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overlayView = findViewById(R.id.overlayView);
        tvScanStatus = findViewById(R.id.tvScanStatus);
        tvTimer = findViewById(R.id.tvTimer);
        btnStartScan = findViewById(R.id.btnStartScan);

        btnStartScan.setOnClickListener(v -> {
            if (!isScanning) {
                startScanSequence();
            }
        });
    }

    private void startScanSequence() {
        isScanning = true;
        btnStartScan.setEnabled(false);
        overlayView.setTargetLocked(true);
        tvScanStatus.setText("Scanning Pupil Response...");

        new CountDownTimer(3000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                float secondsLeft = millisUntilFinished / 1000.0f;
                tvTimer.setText(String.format("00:0%.1f", secondsLeft));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                tvScanStatus.setText("Scan Complete!");
                overlayView.setTargetLocked(false);
                isScanning = false;
                btnStartScan.setEnabled(true);

                Intent intent = new Intent(MainActivity.this, ScanHistoryActivity.class);
                startActivity(intent);
            }
        }.start();
    }
}