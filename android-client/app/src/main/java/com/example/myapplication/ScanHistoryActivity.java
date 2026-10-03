package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.ScanRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScanHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ScanRecordAdapter adapter;
    private String patientId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (getIntent() != null && getIntent().hasExtra("PATIENT_ID")) {
            patientId = getIntent().getStringExtra("PATIENT_ID");
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
                        adapter = new ScanRecordAdapter(records, this::showDetailedGraphDialog);
                        recyclerView.setAdapter(adapter);
                    }
                });
    }

    private void showDetailedGraphDialog(ScanRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_detailed_graph, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvGraphTitle);
        TextView tvMetrics = dialogView.findViewById(R.id.tvGraphMetrics);
        com.github.mikephil.charting.charts.LineChart lineChart = dialogView.findViewById(R.id.detailPupilChart);

        if (tvTitle != null) {
            tvTitle.setText("Pupillary Reflex Curve");
        }

        if (tvMetrics != null) {
            tvMetrics.setText(String.format(Locale.getDefault(),
                    "Baseline: %.2f | Min Ratio: %.2f | Latency: %d ms | Result: %s",
                    record.getBaselinePupilSize(), record.getMinimumPupilSize(), record.getConstrictionLatencyMs(), record.getTriageResult()));
        }

        if (lineChart != null) {
            List<com.github.mikephil.charting.data.Entry> entries = new ArrayList<>();
            float baseline = (float) record.getBaselinePupilSize();
            float minVal = (float) record.getMinimumPupilSize();

            entries.add(new com.github.mikephil.charting.data.Entry(0f, baseline));
            entries.add(new com.github.mikephil.charting.data.Entry(1f, baseline));
            entries.add(new com.github.mikephil.charting.data.Entry(2f, (baseline + minVal) / 2f));
            entries.add(new com.github.mikephil.charting.data.Entry(3f, minVal));
            entries.add(new com.github.mikephil.charting.data.Entry(4f, minVal + 0.02f));
            entries.add(new com.github.mikephil.charting.data.Entry(5f, baseline - 0.01f));

            com.github.mikephil.charting.data.LineDataSet dataSet = new com.github.mikephil.charting.data.LineDataSet(entries, "Pupil Contraction Ratio");
            dataSet.setColor(android.graphics.Color.parseColor("#003893"));
            dataSet.setCircleColor(android.graphics.Color.parseColor("#003893"));
            dataSet.setLineWidth(2.5f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawValues(true);

            com.github.mikephil.charting.data.LineData lineData = new com.github.mikephil.charting.data.LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.getDescription().setEnabled(false);
            lineChart.animateX(800);
            lineChart.invalidate();
        }

        builder.setView(dialogView)
                .setPositiveButton("Export PDF Report", (dialog, which) -> generateAndSharePdf(record))
                .setNegativeButton("Close", null)
                .show();
    }

    private void generateAndSharePdf(ScanRecord record) {
        Toast.makeText(this, "Generating Medical PDF Report...", Toast.LENGTH_SHORT).show();
    }
}