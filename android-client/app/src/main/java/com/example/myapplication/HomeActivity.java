package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.Patient;
import com.example.myapplication.db.ScanRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView tvActivePatientName, tvDashboardTitle, tvSubtitle;
    private TextView tvAnalyticsTotalScans, tvAnalyticsNormal, tvAnalyticsAbnormal, tvAnalyticsAvgLatency;
    private CardView cardSelectedPatient, cardInitiateScan, cardPatientHistory, cardAnalyticsSummary;
    private Button btnRegisterPatient;

    private List<Patient> patientList = new ArrayList<>();
    private Patient activePatient = null;
    private String userRole = "TECHNICIAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (getIntent() != null && getIntent().hasExtra("USER_ROLE")) {
            userRole = getIntent().getStringExtra("USER_ROLE");
        }

        // Bind views
        tvDashboardTitle = findViewById(R.id.tvDashboardTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvActivePatientName = findViewById(R.id.tvActivePatientName);
        cardSelectedPatient = findViewById(R.id.cardSelectedPatient);
        cardInitiateScan = findViewById(R.id.cardInitiateScan);
        cardPatientHistory = findViewById(R.id.cardPatientHistory);
        btnRegisterPatient = findViewById(R.id.btnRegisterPatient);

        // Bind Analytics Views
        cardAnalyticsSummary = findViewById(R.id.cardAnalyticsSummary);
        tvAnalyticsTotalScans = findViewById(R.id.tvAnalyticsTotalScans);
        tvAnalyticsNormal = findViewById(R.id.tvAnalyticsNormal);
        tvAnalyticsAbnormal = findViewById(R.id.tvAnalyticsAbnormal);
        tvAnalyticsAvgLatency = findViewById(R.id.tvAnalyticsAvgLatency);

        configureDashboardForRole();
        loadPatients();
        loadAnalyticsData();

        if (cardSelectedPatient != null) {
            cardSelectedPatient.setOnClickListener(v -> showPatientSelectorDialog());
        }

        if (btnRegisterPatient != null) {
            btnRegisterPatient.setOnClickListener(v -> showRegisterPatientDialog());
        }

        if (cardInitiateScan != null) {
            cardInitiateScan.setOnClickListener(v -> {
                if (activePatient == null) {
                    Toast.makeText(this, "Please select or register a patient profile first", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.putExtra("PATIENT_ID", activePatient.getPatientId());
                startActivity(intent);
            });
        }

        if (cardPatientHistory != null) {
            cardPatientHistory.setOnClickListener(v -> {
                if (activePatient == null) {
                    Toast.makeText(this, "Please select a patient profile first", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(HomeActivity.this, ScanHistoryActivity.class);
                intent.putExtra("PATIENT_ID", activePatient.getPatientId());
                intent.putExtra("USER_ROLE", userRole);
                startActivity(intent);
            });
        }
    }

    private void configureDashboardForRole() {
        if ("PATIENT".equals(userRole)) {
            tvDashboardTitle.setText("Patient Portal");
            tvSubtitle.setText("Select your profile to view history or perform self-scan");
            if (cardAnalyticsSummary != null) cardAnalyticsSummary.setVisibility(View.GONE);
        } else {
            tvDashboardTitle.setText("Technician Dashboard");
            tvSubtitle.setText("Clinical Analytics & Patient Profile Control");
            if (cardAnalyticsSummary != null) cardAnalyticsSummary.setVisibility(View.VISIBLE);
        }
    }

    private void loadAnalyticsData() {
        if (!"TECHNICIAN".equals(userRole)) return;

        AppDatabase.getInstance(this).scanRecordDao().getAllObservableRecords().observe(this, allScans -> {
            if (allScans == null) return;

            int total = allScans.size();
            int normalCount = 0;
            int abnormalCount = 0;
            long totalLatency = 0;

            for (ScanRecord scan : allScans) {
                if ("NORMAL".equalsIgnoreCase(scan.getTriageResult())) {
                    normalCount++;
                } else {
                    abnormalCount++;
                }
                totalLatency += scan.getConstrictionLatencyMs();
            }

            long avgLatency = total > 0 ? (totalLatency / total) : 0;

            if (tvAnalyticsTotalScans != null) tvAnalyticsTotalScans.setText(String.valueOf(total));
            if (tvAnalyticsNormal != null) tvAnalyticsNormal.setText(String.valueOf(normalCount));
            if (tvAnalyticsAbnormal != null) tvAnalyticsAbnormal.setText(String.valueOf(abnormalCount));
            if (tvAnalyticsAvgLatency != null) tvAnalyticsAvgLatency.setText(avgLatency + " ms");
        });
    }

    private void loadPatients() {
        AppDatabase.getInstance(this).patientDao().getAllPatients().observe(this, patients -> {
            if (patients != null) {
                patientList = patients;
                if (!patientList.isEmpty()) {
                    if (activePatient == null) {
                        activePatient = patientList.get(0);
                    }
                    if (tvActivePatientName != null) {
                        tvActivePatientName.setText(activePatient.getFullName() + " (" + activePatient.getPatientId() + ")");
                    }
                } else {
                    activePatient = null;
                    if (tvActivePatientName != null) {
                        tvActivePatientName.setText("No Patients Registered - Tap Register Below");
                    }
                }
            }
        });
    }

    private void showPatientSelectorDialog() {
        if (patientList.isEmpty()) {
            Toast.makeText(this, "No patient profiles found. Please register one first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] patientNames = new String[patientList.size()];
        for (int i = 0; i < patientList.size(); i++) {
            patientNames[i] = patientList.get(i).getFullName() + " [" + patientList.get(i).getPatientId() + "]";
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Patient Profile")
                .setItems(patientNames, (dialog, which) -> {
                    activePatient = patientList.get(which);
                    if (tvActivePatientName != null) {
                        tvActivePatientName.setText(activePatient.getFullName() + " (" + activePatient.getPatientId() + ")");
                    }
                    Toast.makeText(this, "Selected: " + activePatient.getFullName(), Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showRegisterPatientDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register_patient, null);
        EditText etId = dialogView.findViewById(R.id.etRegPatientId);
        EditText etName = dialogView.findViewById(R.id.etRegFullName);
        EditText etAge = dialogView.findViewById(R.id.etRegAge);
        EditText etContact = dialogView.findViewById(R.id.etRegContact);

        if (etId != null) {
            etId.setText("PAT-" + System.currentTimeMillis() % 10000);
        }

        new AlertDialog.Builder(this)
                .setTitle("Register New Patient")
                .setView(dialogView)
                .setPositiveButton("Save Profile", (dialog, which) -> {
                    if (etId == null || etName == null || etAge == null) return;

                    String id = etId.getText().toString().trim();
                    String name = etName.getText().toString().trim();
                    String ageStr = etAge.getText().toString().trim();
                    String contact = (etContact != null) ? etContact.getText().toString().trim() : "";

                    if (id.isEmpty() || name.isEmpty() || ageStr.isEmpty()) {
                        Toast.makeText(this, "ID, Name, and Age are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int age = Integer.parseInt(ageStr);
                    Patient newPatient = new Patient(id, name, age, "Not Specified", contact);

                    new Thread(() -> {
                        AppDatabase.getInstance(this).patientDao().insertPatient(newPatient);
                        runOnUiThread(() -> {
                            activePatient = newPatient;
                            if (tvActivePatientName != null) {
                                tvActivePatientName.setText(activePatient.getFullName() + " (" + activePatient.getPatientId() + ")");
                            }
                            loadAnalyticsData(); // Refresh analytics when adding new patient
                            Toast.makeText(this, "Patient registered successfully", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}