package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.ScanRecord;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConsultantPriorityQueueActivity extends AppCompatActivity {

    private RecyclerView rvConsultantQueue;
    private QueueAdapter adapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        String currentLang = LocaleHelper.getSelectedLanguage(newBase);
        LocaleHelper.setLocale(newBase, currentLang);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultant_queue);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvConsultantQueue = findViewById(R.id.rvConsultantQueue);
        if (rvConsultantQueue != null) {
            rvConsultantQueue.setLayoutManager(new LinearLayoutManager(this));
        }

        loadPriorityQueueData();
    }

    private void loadPriorityQueueData() {
        AppDatabase.getInstance(this).scanRecordDao().getAllObservableRecords().observe(this, records -> {
            if (records != null) {
                // Sort strictly by clinical urgency: UNRESPONSIVE (Red) -> SLUGGISH (Amber) -> NORMAL (Green)
                List<ScanRecord> sortedRecords = new ArrayList<>(records);
                Collections.sort(sortedRecords, new Comparator<ScanRecord>() {
                    @Override
                    public int compare(ScanRecord o1, ScanRecord o2) {
                        return getUrgencyRank(o1.getTriageResult()) - getUrgencyRank(o2.getTriageResult());
                    }
                });

                adapter = new QueueAdapter(sortedRecords, record -> {
                    Intent intent = new Intent(ConsultantPriorityQueueActivity.this, PlrAnalysisActivity.class);
                    intent.putExtra("PATIENT_ID", record.getPatientName());
                    intent.putExtra("TRIAGE_RESULT", record.getTriageResult());
                    intent.putExtra("BASELINE_SIZE", record.getBaselinePupilSize());
                    intent.putExtra("MIN_SIZE", record.getMinimumPupilSize());
                    intent.putExtra("LATENCY_MS", record.getConstrictionLatencyMs());
                    startActivity(intent);
                });
                rvConsultantQueue.setAdapter(adapter);
            }
        });
    }

    private int getUrgencyRank(String verdict) {
        if (verdict == null) return 3;
        String v = verdict.toUpperCase();
        if (v.contains("UNRESPONSIVE") || v.contains("ABNORMAL") || v.contains("HIGH")) return 1;
        if (v.contains("SLUGGISH") || v.contains("OBSERVATION")) return 2;
        return 3; // NORMAL
    }

    // --- Inner Queue Adapter ---
    private static class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {
        private final List<ScanRecord> records;
        private final OnRecordClickListener listener;

        interface OnRecordClickListener {
            void onRecordClick(ScanRecord record);
        }

        QueueAdapter(List<ScanRecord> records, OnRecordClickListener listener) {
            this.records = records != null ? records : new ArrayList<>();
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consultant_queue_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ScanRecord record = records.get(position);
            if (record != null) {
                if (holder.tvPatientName != null) {
                    holder.tvPatientName.setText(record.getPatientName());
                }

                if (holder.tvTimeElapsed != null) {
                    CharSequence elapsed = DateUtils.getRelativeTimeSpanString(record.getScanTimestamp(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                    holder.tvTimeElapsed.setText(elapsed);
                }

                String verdict = record.getTriageResult() != null ? record.getTriageResult().toUpperCase() : "NORMAL";
                if (holder.tvTriagePill != null) {
                    holder.tvTriagePill.setText(verdict);
                    if (verdict.contains("UNRESPONSIVE") || verdict.contains("ABNORMAL")) {
                        holder.tvTriagePill.setTextColor(Color.parseColor("#DC2626"));
                        holder.tvTriagePill.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF2F2")));
                    } else if (verdict.contains("SLUGGISH")) {
                        holder.tvTriagePill.setTextColor(Color.parseColor("#D97706"));
                        holder.tvTriagePill.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
                    } else {
                        holder.tvTriagePill.setTextColor(Color.parseColor("#059669"));
                        holder.tvTriagePill.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ECFDF5")));
                    }
                }

                if (holder.btnReviewCase != null) {
                    holder.btnReviewCase.setOnClickListener(v -> {
                        if (listener != null) listener.onRecordClick(record);
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPatientName, tvTimeElapsed, tvTriagePill, tvSymptomsSummary;
            MaterialButton btnReviewCase;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPatientName = itemView.findViewById(R.id.tvPatientName);
                tvTimeElapsed = itemView.findViewById(R.id.tvTimeElapsed);
                tvTriagePill = itemView.findViewById(R.id.tvTriagePill);
                tvSymptomsSummary = itemView.findViewById(R.id.tvSymptomsSummary);
                btnReviewCase = itemView.findViewById(R.id.btnReviewCase);
            }
        }
    }
}