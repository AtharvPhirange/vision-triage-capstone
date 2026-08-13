package com.example.myapplication.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ScanResultActivity extends AppCompatActivity {

    private TextView tvPatientName;
    private TextView tvTriageResult;
    private TextView tvBaseline;
    private TextView tvMinimum;
    private TextView tvLatency;
    private LineChart pupilChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        tvPatientName = findViewById(R.id.tvPatientName);
        tvTriageResult = findViewById(R.id.tvTriageResult);
        tvBaseline = findViewById(R.id.tvBaseline);
        tvMinimum = findViewById(R.id.tvMinimum);
        tvLatency = findViewById(R.id.tvLatency);
        pupilChart = findViewById(R.id.pupilChart);

        setupChartStyle();
        loadIntentData();
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            String patientName = intent.getStringExtra("PATIENT_NAME");
            String triageResult = intent.getStringExtra("TRIAGE_RESULT");
            double baseline = intent.getDoubleExtra("BASELINE", 0.0);
            double minimum = intent.getDoubleExtra("MINIMUM", 0.0);
            long latency = intent.getLongExtra("LATENCY", 0);

            List<Entry> chartPoints = generateMockCurve(baseline, minimum, latency);
            displayScanData(patientName, triageResult, baseline, minimum, latency, chartPoints);
        }
    }

    private List<Entry> generateMockCurve(double baseline, double minimum, long latencyMs) {
        List<Entry> entries = new ArrayList<>();
        float latencySec = latencyMs / 1000.0f;

        for (float t = 0; t <= 2.0f; t += 0.05f) {
            float val;
            if (t < latencySec) {
                val = (float) baseline;
            } else if (t < latencySec + 0.5f) {
                float progress = (t - latencySec) / 0.5f;
                val = (float) (baseline - (baseline - minimum) * progress);
            } else {
                float progress = (t - (latencySec + 0.5f)) / 1.0f;
                val = (float) (minimum + (baseline - minimum) * 0.3f * progress);
            }
            entries.add(new Entry(t, val));
        }
        return entries;
    }

    private void setupChartStyle() {
        pupilChart.setBackgroundColor(Color.TRANSPARENT);
        pupilChart.getDescription().setEnabled(false);
        pupilChart.setTouchEnabled(true);
        pupilChart.setDragEnabled(true);
        pupilChart.setScaleEnabled(true);
        pupilChart.setPinchZoom(true);

        XAxis xAxis = pupilChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = pupilChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.DKGRAY);

        pupilChart.getAxisRight().setEnabled(false);
        pupilChart.getLegend().setTextColor(Color.WHITE);
    }

    public void displayScanData(String patientName, String triageResult, double baseline, double minimum, long latencyMs, List<Entry> timeSeriesPoints) {
        tvPatientName.setText("Patient: " + (patientName != null ? patientName : "Unknown"));
        tvTriageResult.setText(triageResult != null ? triageResult : "UNKNOWN");
        tvBaseline.setText(String.format("%.1f mm", baseline));
        tvMinimum.setText(String.format("%.1f mm", minimum));
        tvLatency.setText(latencyMs + " ms");

        if ("ABNORMAL".equalsIgnoreCase(triageResult)) {
            tvTriageResult.setTextColor(Color.RED);
        } else {
            tvTriageResult.setTextColor(Color.GREEN);
        }

        renderChart(timeSeriesPoints);
    }

    private void renderChart(List<Entry> entries) {
        if (entries == null || entries.isEmpty()) {
            entries = new ArrayList<>();
        }

        LineDataSet dataSet = new LineDataSet(entries, "Pupil Diameter (mm)");
        dataSet.setColor(Color.CYAN);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(Color.CYAN);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(9f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        pupilChart.setData(lineData);
        pupilChart.animateX(800);
        pupilChart.invalidate();
    }
}
