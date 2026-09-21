package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ScanResultActivity extends AppCompatActivity {

    private LineChart lineChart;
    private TextView tvPatientNameResult, tvTriageBadge, tvBaselineVal, tvMinVal, tvLatencyVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        lineChart = findViewById(R.id.lineChart);
        tvPatientNameResult = findViewById(R.id.tvPatientNameResult);
        tvTriageBadge = findViewById(R.id.tvTriageBadge);
        tvBaselineVal = findViewById(R.id.tvBaselineVal);
        tvMinVal = findViewById(R.id.tvMinVal);
        tvLatencyVal = findViewById(R.id.tvLatencyVal);

        String name = getIntent().getStringExtra("PATIENT_NAME");
        String result = getIntent().getStringExtra("TRIAGE_RESULT");
        float baseline = getIntent().getFloatExtra("BASELINE", 0f);
        float minimum = getIntent().getFloatExtra("MINIMUM", 0f);
        long latency = getIntent().getLongExtra("LATENCY", 0L);

        tvPatientNameResult.setText(name != null ? name : "Unknown Patient");
        tvTriageBadge.setText(result != null ? result : "PENDING");
        tvBaselineVal.setText(baseline + " mm");
        tvMinVal.setText(minimum + " mm");
        tvLatencyVal.setText(latency + " ms");

        setupChart();
    }

    private void setupChart() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, 5.0f));
        entries.add(new Entry(1f, 4.8f));
        entries.add(new Entry(2f, 3.2f));
        entries.add(new Entry(3f, 3.5f));
        entries.add(new Entry(4f, 4.6f));
        entries.add(new Entry(5f, 4.9f));

        LineDataSet dataSet = new LineDataSet(entries, "Pupillary Response (mm)");
        dataSet.setColor(getResources().getColor(android.R.color.holo_green_light));
        dataSet.setValueTextColor(getResources().getColor(android.R.color.white));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setCircleColor(getResources().getColor(android.R.color.holo_green_dark));

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.setDescription(null);
        lineChart.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        lineChart.invalidate();
    }
}