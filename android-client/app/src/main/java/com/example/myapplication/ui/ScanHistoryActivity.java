package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ScanRecord;
import com.example.myapplication.ScanRepository;

import java.util.List;

public class ScanHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ScanRecord> records = ScanRepository.getInstance().getScanRecords();

        ScanAdapter adapter = new ScanAdapter(records, record -> {
            Intent intent = new Intent(ScanHistoryActivity.this, ScanResultActivity.class);
            intent.putExtra("PATIENT_NAME", record.getPatientName());
            intent.putExtra("TRIAGE_RESULT", record.getTriageResult());
            intent.putExtra("BASELINE", record.getBaselinePupilSize());
            intent.putExtra("MINIMUM", record.getMinimumPupilSize());
            intent.putExtra("LATENCY", record.getConstrictionLatencyMs());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }
}