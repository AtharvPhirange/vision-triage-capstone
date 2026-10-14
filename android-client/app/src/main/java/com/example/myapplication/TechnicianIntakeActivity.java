package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.Patient;

public class TechnicianIntakeActivity extends AppCompatActivity {

    private TextInputEditText etPatientId, etFullName, etAge, etGender, etHeartRate, etBp;
    private ChipGroup chipGroupSymptoms;

    @Override
    protected void attachBaseContext(Context newBase) {
        String currentLang = LocaleHelper.getSelectedLanguage(newBase);
        LocaleHelper.setLocale(newBase, currentLang);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_intake);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        etPatientId = findViewById(R.id.etPatientId);
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etGender = findViewById(R.id.etGender);
        chipGroupSymptoms = findViewById(R.id.chipGroupSymptoms);
        etHeartRate = findViewById(R.id.etHeartRate);
        etBp = findViewById(R.id.etBp);

        MaterialButton btnProceedToScan = findViewById(R.id.btnProceedToScan);

        // Pre-fill active patient demographics
        loadActivePatientData();

        if (btnProceedToScan != null) {
            btnProceedToScan.setOnClickListener(v -> proceedToScan());
        }
    }

    private void loadActivePatientData() {
        String activeId = PatientSessionManager.getActivePatientId(this);
        new Thread(() -> {
            Patient patient = AppDatabase.getInstance(TechnicianIntakeActivity.this)
                    .patientDao()
                    .getPatientById(activeId);

            runOnUiThread(() -> {
                if (patient != null) {
                    if (etPatientId != null) etPatientId.setText(patient.getPatientId());
                    if (etFullName != null) etFullName.setText(patient.getFullName());
                    if (etAge != null) etAge.setText(String.valueOf(patient.getAge()));
                    if (etGender != null) etGender.setText(patient.getGender());
                } else {
                    if (etPatientId != null) etPatientId.setText(activeId);
                }
            });
        }).start();
    }

    private void proceedToScan() {
        String patientId = etPatientId != null && etPatientId.getText() != null ? etPatientId.getText().toString().trim() : "PAT-9105";
        String fullName = etFullName != null && etFullName.getText() != null ? etFullName.getText().toString().trim() : "Unknown";

        // Collect Checked Symptoms
        StringBuilder symptomsBuilder = new StringBuilder();
        if (chipGroupSymptoms != null) {
            for (int i = 0; i < chipGroupSymptoms.getChildCount(); i++) {
                View view = chipGroupSymptoms.getChildAt(i);
                if (view instanceof Chip) {
                    Chip chip = (Chip) view;
                    if (chip.isChecked()) {
                        if (symptomsBuilder.length() > 0) symptomsBuilder.append(", ");
                        symptomsBuilder.append(chip.getText());
                    }
                }
            }
        }

        String symptoms = symptomsBuilder.toString();
        String heartRate = etHeartRate != null && etHeartRate.getText() != null ? etHeartRate.getText().toString().trim() : "72";
        String bp = etBp != null && etBp.getText() != null ? etBp.getText().toString().trim() : "120/80";

        Toast.makeText(this, "Intake Recorded. Starting Scan...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("PATIENT_ID", patientId);
        intent.putExtra("PATIENT_NAME", fullName);
        intent.putExtra("SYMPTOMS", symptoms);
        intent.putExtra("HEART_RATE", heartRate);
        intent.putExtra("BP", bp);
        startActivity(intent);
        finish();
    }
}