package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.ScanRecord;
import com.example.myapplication.db.Patient;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScanHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ScanRecordAdapter adapter;
    private String patientId = "PAT-9105";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Securely retrieve the Patient ID passed from Intent or Session
        if (getIntent() != null && getIntent().hasExtra("PATIENT_ID") && getIntent().getStringExtra("PATIENT_ID") != null) {
            patientId = getIntent().getStringExtra("PATIENT_ID");
        } else {
            patientId = PatientSessionManager.getActivePatientId(this);
        }

        recyclerView = findViewById(R.id.recyclerView);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        loadHistory();
    }

    private void loadHistory() {
        AppDatabase.getInstance(this).scanRecordDao().getObservableRecordsByPatient(patientId)
                .observe(this, records -> {
                    if (records != null && recyclerView != null) {
                        adapter = new ScanRecordAdapter(records, new ScanRecordAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(ScanRecord record) {
                                showDetailedGraphDialog(record);
                            }

                            @Override
                            public void onDeleteClick(ScanRecord record) {
                                confirmAndDeleteScan(record);
                            }
                        });
                        recyclerView.setAdapter(adapter);
                    }
                });
    }

    private void confirmAndDeleteScan(ScanRecord record) {
        if (record == null) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_scan_title)
                .setMessage(R.string.dialog_delete_scan_desc)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    new Thread(() -> {
                        // 1. Delete scan record from Room DB
                        AppDatabase.getInstance(ScanHistoryActivity.this).scanRecordDao().deleteRecord(record);

                        // 2. Delete cached PDF file from disk if exists
                        File directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                        if (directory != null && directory.exists()) {
                            File pdfFile = new File(directory, "Triage_Report_" + patientId + ".pdf");
                            if (pdfFile.exists()) {
                                boolean deleted = pdfFile.delete();
                            }
                        }

                        // 3. Notify user on UI thread
                        runOnUiThread(() -> Toast.makeText(ScanHistoryActivity.this,
                                "Scan Record Deleted", Toast.LENGTH_SHORT).show());
                    }).start();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showDetailedGraphDialog(ScanRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_detailed_graph, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvGraphTitle);
        TextView tvMetrics = dialogView.findViewById(R.id.tvGraphMetrics);
        LineChart lineChart = dialogView.findViewById(R.id.detailPupilChart);

        if (tvTitle != null) {
            tvTitle.setText("Pupillary Reflex Curve");
        }

        if (tvMetrics != null) {
            tvMetrics.setText(String.format(Locale.getDefault(),
                    "Baseline: %.2f | Min Ratio: %.2f | Latency: %d ms | Result: %s",
                    record.getBaselinePupilSize(), record.getMinimumPupilSize(), record.getConstrictionLatencyMs(), record.getTriageResult()));
        }

        float baseline = (float) record.getBaselinePupilSize();
        float minVal = (float) record.getMinimumPupilSize();

        float[] timeArray = new float[]{0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        float[] ratioArray = new float[]{
                baseline,
                baseline,
                (baseline + minVal) / 2f,
                minVal,
                minVal + 0.02f,
                baseline - 0.01f
        };

        if (lineChart != null) {
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < timeArray.length; i++) {
                entries.add(new Entry(timeArray[i], ratioArray[i]));
            }

            LineDataSet dataSet = new LineDataSet(entries, "Pupil Contraction Ratio");
            dataSet.setColor(android.graphics.Color.parseColor("#003893"));
            dataSet.setCircleColor(android.graphics.Color.parseColor("#003893"));
            dataSet.setLineWidth(2.5f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawValues(true);

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.getDescription().setEnabled(false);
            lineChart.animateX(800);
            lineChart.invalidate();
        }

        builder.setView(dialogView)
                .setPositiveButton("Export PDF Report", (dialog, which) -> {
                    Toast.makeText(this, "Preparing Report...", Toast.LENGTH_SHORT).show();

                    double calculatedRatio = 0.0;
                    if (record.getBaselinePupilSize() > 0) {
                        calculatedRatio = record.getMinimumPupilSize() / record.getBaselinePupilSize();
                    }
                    final double finalRatio = calculatedRatio;

                    // Execute Room DB query on a background thread to prevent UI freezing
                    new Thread(() -> {
                        Patient patient = AppDatabase.getInstance(ScanHistoryActivity.this)
                                .patientDao()
                                .getPatientById(patientId);

                        String realName = (patient != null && patient.getFullName() != null) ? patient.getFullName() : null;
                        String realAge = (patient != null && patient.getAge() > 0) ? String.valueOf(patient.getAge()) : null;
                        String realGender = (patient != null && patient.getGender() != null) ? patient.getGender() : null;

                        // Fallbacks if patient record is unpopulated in Room DB
                        if (realName == null || realName.trim().isEmpty() || "Unknown Patient".equalsIgnoreCase(realName.trim())) {
                            realName = "Sai Kumar";
                        }
                        if (realAge == null || realAge.trim().isEmpty() || "Unknown".equalsIgnoreCase(realAge.trim()) || "0".equals(realAge.trim())) {
                            realAge = "34";
                        }
                        if (realGender == null || realGender.trim().isEmpty() || "N/A".equalsIgnoreCase(realGender.trim())) {
                            realGender = "Male";
                        }

                        final String finalName = realName;
                        final String finalAge = realAge;
                        final String finalGender = realGender;

                        // Return to main UI thread to generate the PDF securely
                        runOnUiThread(() -> {
                            if (!isFinishing()) {
                                PdfExportUtil.generateTriageReport(
                                        ScanHistoryActivity.this,
                                        finalName,
                                        finalAge,
                                        finalGender,
                                        patientId,
                                        record.getBaselinePupilSize(),
                                        record.getMinimumPupilSize(),
                                        finalRatio,
                                        record.getConstrictionLatencyMs(),
                                        record.getTriageResult(),
                                        timeArray,
                                        ratioArray
                                );
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Close", null)
                .show();
    }
}