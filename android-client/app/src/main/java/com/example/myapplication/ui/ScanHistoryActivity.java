package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ScanRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScanHistoryActivity extends AppCompatActivity {

    private RecyclerView rvScanHistory;
    private ScanAdapter scanAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_history);

        rvScanHistory = findViewById(R.id.rvScanHistory);
        rvScanHistory.setLayoutManager(new LinearLayoutManager(this));

        scanAdapter = new ScanAdapter(new ArrayList<>(), record -> {
            Intent intent = new Intent(ScanHistoryActivity.this, ScanResultActivity.class);
            intent.putExtra("SCAN_ID", record.getScanId());
            intent.putExtra("PATIENT_NAME", record.getPatientName());
            intent.putExtra("TRIAGE_RESULT", record.getTriageResult());
            intent.putExtra("BASELINE", record.getBaselinePupilSize());
            intent.putExtra("MINIMUM", record.getMinimumPupilSize());
            intent.putExtra("LATENCY", record.getConstrictionLatencyMs());
            startActivity(intent);
        });

        rvScanHistory.setAdapter(scanAdapter);
        loadMockData();
    }

    private void loadMockData() {
        List<ScanRecord> mockList = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        mockList.add(new ScanRecord(
                UUID.randomUUID().toString(),
                "John Doe",
                currentTime - 3600000L,
                4.8,
                2.5,
                220L,
                "NORMAL",
                true
        ));

        mockList.add(new ScanRecord(
                UUID.randomUUID().toString(),
                "Jane Smith",
                currentTime - 7200000L,
                5.2,
                4.6,
                490L,
                "ABNORMAL",
                false
        ));

        mockList.add(new ScanRecord(
                UUID.randomUUID().toString(),
                "Alex Johnson",
                currentTime - 86400000L,
                4.5,
                2.2,
                205L,
                "NORMAL",
                true
        ));

        mockList.add(new ScanRecord(
                UUID.randomUUID().toString(),
                "Sam Wilson",
                currentTime - 172800000L,
                5.0,
                4.7,
                530L,
                "ABNORMAL",
                true
        ));

        scanAdapter.updateData(mockList);
    }
}