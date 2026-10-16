package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.Patient;
import com.example.myapplication.db.ScanRecord;

import java.util.ArrayList;
import java.util.List;

public class TechnicianScanArchiveActivity extends BaseActivity {

    private RecyclerView rvTechnicianArchive;
    private TextView tvEmptyArchive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_scan_archive);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvTechnicianArchive = findViewById(R.id.rvTechnicianArchive);
        tvEmptyArchive = findViewById(R.id.tvEmptyArchive);

        if (rvTechnicianArchive != null) {
            rvTechnicianArchive.setLayoutManager(new LinearLayoutManager(this));
        }

        loadTechnicianScans();
    }

    private void loadTechnicianScans() {
        AppDatabase.getInstance(this).scanRecordDao().getAllObservableRecords().observe(this, records -> {
            List<ScanRecord> techScans = new ArrayList<>();
            if (records != null) {
                for (ScanRecord r : records) {
                    if ("TECHNICIAN".equalsIgnoreCase(r.getScanSource())) {
                        techScans.add(r);
                    }
                }
            }

            if (techScans.isEmpty()) {
                if (tvEmptyArchive != null) tvEmptyArchive.setVisibility(View.VISIBLE);
                if (rvTechnicianArchive != null) rvTechnicianArchive.setVisibility(View.GONE);
            } else {
                if (tvEmptyArchive != null) tvEmptyArchive.setVisibility(View.GONE);
                if (rvTechnicianArchive != null) rvTechnicianArchive.setVisibility(View.VISIBLE);

                ScanRecordAdapter adapter = new ScanRecordAdapter(techScans, new ScanRecordAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(ScanRecord record) {
                        Intent intent = new Intent(TechnicianScanArchiveActivity.this, PlrAnalysisActivity.class);
                        intent.putExtra("PATIENT_ID", record.getPatientName());
                        intent.putExtra("TRIAGE_RESULT", record.getTriageResult());
                        intent.putExtra("BASELINE_SIZE", record.getBaselinePupilSize());
                        intent.putExtra("MIN_SIZE", record.getMinimumPupilSize());
                        intent.putExtra("LATENCY_MS", record.getConstrictionLatencyMs());
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteClick(ScanRecord record) {
                        Toast.makeText(TechnicianScanArchiveActivity.this, "Generating Clinical PDF Report...", Toast.LENGTH_SHORT).show();
                        generatePdfForRecord(record);
                    }
                });
                rvTechnicianArchive.setAdapter(adapter);
            }
        });
    }

    private void generatePdfForRecord(ScanRecord record) {
        if (record == null) return;
        new Thread(() -> {
            Patient patient = AppDatabase.getInstance(TechnicianScanArchiveActivity.this)
                    .patientDao()
                    .getPatientById(record.getPatientName());

            String name = patient != null ? patient.getFullName() : "Field Patient";
            String age = patient != null ? String.valueOf(patient.getAge()) : "N/A";
            String gender = patient != null ? patient.getGender() : "N/A";

            double ratio = record.getBaselinePupilSize() > 0 ? record.getMinimumPupilSize() / record.getBaselinePupilSize() : 0.65;

            runOnUiThread(() -> PdfExportUtil.generateTriageReport(
                    TechnicianScanArchiveActivity.this,
                    name,
                    age,
                    gender,
                    record.getPatientName(),
                    record.getBaselinePupilSize(),
                    record.getMinimumPupilSize(),
                    ratio,
                    record.getConstrictionLatencyMs(),
                    record.getTriageResult()
            ));
        }).start();
    }
}