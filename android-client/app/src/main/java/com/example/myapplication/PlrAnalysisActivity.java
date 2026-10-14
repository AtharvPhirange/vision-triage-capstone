package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.Patient;
import java.util.Locale;

public class PlrAnalysisActivity extends AppCompatActivity {

    private String patientId = "PAT-9105";
    private PlrWaveformView plrWaveformView;

    @Override
    protected void attachBaseContext(Context newBase) {
        String currentLang = LocaleHelper.getSelectedLanguage(newBase);
        LocaleHelper.setLocale(newBase, currentLang);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plr_analysis);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (getIntent() != null && getIntent().hasExtra("PATIENT_ID") && getIntent().getStringExtra("PATIENT_ID") != null) {
            patientId = getIntent().getStringExtra("PATIENT_ID");
        }

        TextView tvPatientDisplayName = findViewById(R.id.tvPatientDisplayName);
        TextView tvAiVerdictBadge = findViewById(R.id.tvAiVerdictBadge);
        TextView tvBaselineVal = findViewById(R.id.tvBaselineVal);
        TextView tvMinVal = findViewById(R.id.tvMinVal);
        TextView tvLatencyVal = findViewById(R.id.tvLatencyVal);

        plrWaveformView = findViewById(R.id.plrWaveformView);
        MaterialButton btnPlayAnimation = findViewById(R.id.btnPlayAnimation);

        RadioGroup rgOverride = findViewById(R.id.rgOverride);
        TextInputEditText etClinicalNotes = findViewById(R.id.etClinicalNotes);

        MaterialButton btnReferUrgent = findViewById(R.id.btnReferUrgent);
        MaterialButton btnScheduleVideo = findViewById(R.id.btnScheduleVideo);
        MaterialButton btnDischargePatient = findViewById(R.id.btnDischargePatient);

        // Retrieve passed parameters
        double baseVal = getIntent() != null ? getIntent().getDoubleExtra("BASELINE_SIZE", 5.20) : 5.20;
        double minVal = getIntent() != null ? getIntent().getDoubleExtra("MIN_SIZE", 0.12) : 0.12;
        long latencyMs = getIntent() != null ? getIntent().getLongExtra("LATENCY_MS", 0) : 0;
        String triageResult = getIntent() != null && getIntent().hasExtra("TRIAGE_RESULT") ? getIntent().getStringExtra("TRIAGE_RESULT") : "UNRESPONSIVE";

        if (tvBaselineVal != null) tvBaselineVal.setText(String.format(Locale.getDefault(), "%.2f mm", baseVal));
        if (tvMinVal != null) tvMinVal.setText(String.format(Locale.getDefault(), "%.2f", minVal));
        if (tvLatencyVal != null) tvLatencyVal.setText(latencyMs + " ms");
        if (tvAiVerdictBadge != null) tvAiVerdictBadge.setText(triageResult);

        if (plrWaveformView != null) {
            plrWaveformView.setWaveformMetrics((float) baseVal, (float) minVal);
        }

        if (btnPlayAnimation != null && plrWaveformView != null) {
            btnPlayAnimation.setOnClickListener(v -> plrWaveformView.startPlaybackAnimation());
        }

        // Fetch Patient Name
        new Thread(() -> {
            Patient patient = AppDatabase.getInstance(PlrAnalysisActivity.this).patientDao().getPatientById(patientId);
            runOnUiThread(() -> {
                if (tvPatientDisplayName != null) {
                    if (patient != null) {
                        tvPatientDisplayName.setText(patient.getFullName() + " (" + patient.getPatientId() + ")");
                    } else {
                        tvPatientDisplayName.setText(patientId);
                    }
                }
            });
        }).start();

        // Disposition Actions
        if (btnReferUrgent != null) {
            btnReferUrgent.setOnClickListener(v -> {
                Toast.makeText(this, "Generating Urgent Transfer Requisition & Rejection PDF...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ScanHistoryActivity.class);
                intent.putExtra("PATIENT_ID", patientId);
                startActivity(intent);
                finish();
            });
        }

        if (btnScheduleVideo != null) {
            btnScheduleVideo.setOnClickListener(v -> {
                Toast.makeText(this, "Tele-Ophthalmology Follow-up Slot Scheduled", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        if (btnDischargePatient != null) {
            btnDischargePatient.setOnClickListener(v -> {
                Toast.makeText(this, "Case Marked Resolved. Discharge Summary Sent.", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
}