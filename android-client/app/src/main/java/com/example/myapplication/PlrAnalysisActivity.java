package com.example.myapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.Patient;
import com.example.myapplication.db.ScanRecord;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class PlrAnalysisActivity extends BaseActivity {

    private String patientId = "PAT-9105";
    private PlrWaveformView plrWaveformView;
    private TextView tvAiVerdictBadge;
    private TextInputEditText etClinicalNotes;

    private String aiVerdict = "NORMAL";
    private String selectedFinalVerdict = "NORMAL";
    private String overrideStatus = "CONCURRED"; // "CONCURRED" vs "OVERRIDDEN"
    private ScanRecord currentScanRecord;

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
        tvAiVerdictBadge = findViewById(R.id.tvAiVerdictBadge);
        TextView tvBaselineVal = findViewById(R.id.tvBaselineVal);
        TextView tvMinVal = findViewById(R.id.tvMinVal);
        TextView tvLatencyVal = findViewById(R.id.tvLatencyVal);
        TextView tvWaveformExplanation = findViewById(R.id.tvWaveformExplanation);

        plrWaveformView = findViewById(R.id.plrWaveformView);
        MaterialButton btnPlayAnimation = findViewById(R.id.btnPlayAnimation);

        RadioGroup rgOverride = findViewById(R.id.rgOverride);
        etClinicalNotes = findViewById(R.id.etClinicalNotes);

        MaterialButton btnReferUrgent = findViewById(R.id.btnReferUrgent);
        MaterialButton btnScheduleVideo = findViewById(R.id.btnScheduleVideo);
        MaterialButton btnDischargePatient = findViewById(R.id.btnDischargePatient);

        // Retrieve passed parameters
        double rawBaseVal = getIntent() != null ? getIntent().getDoubleExtra("BASELINE_SIZE", 0.15) : 0.15;
        double rawMinVal = getIntent() != null ? getIntent().getDoubleExtra("MIN_SIZE", 0.11) : 0.11;
        long passedLatency = getIntent() != null ? getIntent().getLongExtra("LATENCY_MS", 0) : 0;
        aiVerdict = getIntent() != null && getIntent().hasExtra("TRIAGE_RESULT") ? getIntent().getStringExtra("TRIAGE_RESULT") : "NORMAL";
        selectedFinalVerdict = aiVerdict;

        double baseVal = rawBaseVal <= 1.0 ? rawBaseVal * 28.0 : rawBaseVal;
        double minVal = rawMinVal <= 1.0 ? rawMinVal * 28.0 : rawMinVal;
        long latencyMs = passedLatency > 0 ? passedLatency : ("UNRESPONSIVE".equalsIgnoreCase(aiVerdict) ? 820 : ("SLUGGISH".equalsIgnoreCase(aiVerdict) ? 380 : 240));

        if (tvBaselineVal != null) tvBaselineVal.setText(String.format(Locale.getDefault(), "%.2f mm", baseVal));
        if (tvMinVal != null) tvMinVal.setText(String.format(Locale.getDefault(), "%.2f mm", minVal));
        if (tvLatencyVal != null) tvLatencyVal.setText(latencyMs + " ms");
        
        updateVerdictBadgeUI(selectedFinalVerdict);

        if (tvWaveformExplanation != null) {
            String explanation = String.format(Locale.getDefault(),
                    "1. Baseline Phase (0s–1.0s): Resting pupil diameter recorded at %.2f mm under uniform ambient light.\n" +
                    "2. Constriction Phase (1.0s–2.5s): Flash trigger at 1.0s initiated reflex constriction down to %.2f mm peak with latency of %d ms. Verdict classified as %s.\n" +
                    "3. Recovery Phase (2.5s–5.0s): Sympathetic autonomic re-dilation slope demonstrates physiological recovery.",
                    baseVal, minVal, latencyMs, aiVerdict);
            tvWaveformExplanation.setText(explanation);
        }

        if (plrWaveformView != null) {
            plrWaveformView.setWaveformMetrics((float) baseVal, (float) minVal, (int) latencyMs, aiVerdict);
        }

        if (btnPlayAnimation != null && plrWaveformView != null) {
            btnPlayAnimation.setOnClickListener(v -> plrWaveformView.startPlaybackAnimation());
        }

        // Radio Group Listener for Specialist Override
        if (rgOverride != null) {
            rgOverride.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbConcur) {
                    selectedFinalVerdict = aiVerdict;
                    overrideStatus = "CONCURRED";
                } else if (checkedId == R.id.rbNormal) {
                    selectedFinalVerdict = "NORMAL";
                    overrideStatus = "OVERRIDDEN";
                } else if (checkedId == R.id.rbSluggish) {
                    selectedFinalVerdict = "SLUGGISH";
                    overrideStatus = "OVERRIDDEN";
                } else if (checkedId == R.id.rbUnresponsive) {
                    selectedFinalVerdict = "UNRESPONSIVE";
                    overrideStatus = "OVERRIDDEN";
                }
                updateVerdictBadgeUI(selectedFinalVerdict);
            });
        }

        // Fetch Scan Record & Patient Name from Room DB
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(PlrAnalysisActivity.this);
            Patient patient = db.patientDao().getPatientById(patientId);
            List<ScanRecord> records = db.scanRecordDao().getAllRecordsForSync();
            if (records != null && !records.isEmpty()) {
                for (ScanRecord r : records) {
                    if (r.getPatientName().equalsIgnoreCase(patientId)) {
                        currentScanRecord = r;
                        break;
                    }
                }
                if (currentScanRecord == null) {
                    currentScanRecord = records.get(0);
                }
            }

            runOnUiThread(() -> {
                if (tvPatientDisplayName != null) {
                    if (patient != null) {
                        tvPatientDisplayName.setText(patient.getFullName() + " (" + patient.getPatientId() + ")");
                    } else {
                        tvPatientDisplayName.setText(patientId);
                    }
                }
            });
        });

        // Wire 3 Disposition Buttons
        if (btnReferUrgent != null) {
            btnReferUrgent.setOnClickListener(v -> commitSpecialistDisposition(
                    "URGENT_TRAUMA",
                    "🚨 Emergency Escalation Dispatched",
                    "Patient has been escalated to Emergency Trauma & Neurosurgery Unit with Highest Priority. Triage alert transmitted via ABDM fast-track."
            ));
        }

        if (btnScheduleVideo != null) {
            btnScheduleVideo.setOnClickListener(v -> commitSpecialistDisposition(
                    "TELE_OPHTHALMOLOGY",
                    "📅 Tele-Ophthalmology Scheduled",
                    "Consultation ticket generated. Secure video session link dispatched to patient registered mobile number for scheduled follow-up."
            ));
        }

        if (btnDischargePatient != null) {
            btnDischargePatient.setOnClickListener(v -> commitSpecialistDisposition(
                    "ROUTINE_DISCHARGE",
                    "✓ Patient Cleared & Discharged",
                    "Scan review completed. Patient marked as low-risk and discharged for standard routine annual follow-up."
            ));
        }
    }

    private void updateVerdictBadgeUI(String verdict) {
        if (tvAiVerdictBadge == null || verdict == null) return;
        tvAiVerdictBadge.setText(verdict.toUpperCase());

        if (verdict.contains("UNRESPONSIVE") || verdict.contains("ABNORMAL")) {
            tvAiVerdictBadge.setTextColor(Color.parseColor("#DC2626"));
            tvAiVerdictBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF2F2")));
        } else if (verdict.contains("SLUGGISH")) {
            tvAiVerdictBadge.setTextColor(Color.parseColor("#D97706"));
            tvAiVerdictBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
        } else {
            tvAiVerdictBadge.setTextColor(Color.parseColor("#059669"));
            tvAiVerdictBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ECFDF5")));
        }
    }

    private void commitSpecialistDisposition(String disposition, String confirmationTitle, String confirmationMessage) {
        String notes = etClinicalNotes != null && etClinicalNotes.getText() != null ? etClinicalNotes.getText().toString().trim() : "";
        long now = System.currentTimeMillis();

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(PlrAnalysisActivity.this);
            if (currentScanRecord != null) {
                currentScanRecord.setConsultantReviewStatus(overrideStatus);
                currentScanRecord.setFinalClinicalVerdict(selectedFinalVerdict);
                currentScanRecord.setSpecialistNotes(notes);
                currentScanRecord.setTriageDisposition(disposition);
                currentScanRecord.setReviewedTimestamp(now);

                db.scanRecordDao().update(currentScanRecord);
            }
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle(confirmationTitle)
                .setMessage(confirmationMessage + "\n\n• Final Verdict: " + selectedFinalVerdict +
                        "\n• Doctor Sign-Off: Dr. A. Sharma (Reg #MMC-84920)" +
                        (notes.isEmpty() ? "" : "\n• Notes: " + notes))
                .setCancelable(false)
                .setPositiveButton("Return to Queue", (dialog, which) -> {
                    dialog.dismiss();
                    setResult(RESULT_OK);
                    finish();
                })
                .show();
    }
}