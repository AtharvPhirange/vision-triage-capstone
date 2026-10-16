package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.ScanSyncQueue;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SyncQueueActivity extends BaseActivity {

    private TextView tvPendingCount, tvEmptyQueue;
    private RecyclerView rvSyncQueue;
    private SyncQueueAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_queue);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvEmptyQueue = findViewById(R.id.tvEmptyQueue);
        rvSyncQueue = findViewById(R.id.rvSyncQueue);

        if (rvSyncQueue != null) {
            rvSyncQueue.setLayoutManager(new LinearLayoutManager(this));
        }

        MaterialButton btnSyncNow = findViewById(R.id.btnSyncNow);
        if (btnSyncNow != null) {
            btnSyncNow.setOnClickListener(v -> triggerManualSync());
        }

        loadQueueFromDatabase();
    }

    private void loadQueueFromDatabase() {
        AppDatabase.getInstance(this).scanSyncDao().getObservableSyncQueue().observe(this, queueItems -> {
            if (queueItems == null || queueItems.isEmpty()) {
                if (tvPendingCount != null) tvPendingCount.setText("0 Pending Uploads");
                if (tvEmptyQueue != null) tvEmptyQueue.setVisibility(View.VISIBLE);
                if (rvSyncQueue != null) rvSyncQueue.setVisibility(View.GONE);
            } else {
                int pendingCount = 0;
                for (ScanSyncQueue item : queueItems) {
                    if ("PENDING_SYNC".equalsIgnoreCase(item.getSyncStatus())) {
                        pendingCount++;
                    }
                }

                if (tvPendingCount != null) {
                    tvPendingCount.setText(pendingCount + " Pending Uploads");
                }
                if (tvEmptyQueue != null) tvEmptyQueue.setVisibility(View.GONE);
                if (rvSyncQueue != null) rvSyncQueue.setVisibility(View.VISIBLE);

                adapter = new SyncQueueAdapter(queueItems);
                rvSyncQueue.setAdapter(adapter);
            }
        });
    }

    private void triggerManualSync() {
        Toast.makeText(this, "Enqueuing WorkManager Sync...", Toast.LENGTH_SHORT).show();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueue(syncRequest);
    }

    // --- Inner Adapter Class ---
    private static class SyncQueueAdapter extends RecyclerView.Adapter<SyncQueueAdapter.ViewHolder> {
        private final List<ScanSyncQueue> items;

        SyncQueueAdapter(List<ScanSyncQueue> items) {
            this.items = items != null ? items : new ArrayList<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sync_queue, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ScanSyncQueue item = items.get(position);
            if (item != null) {
                if (holder.tvItemName != null) {
                    holder.tvItemName.setText(item.getPatientName() + " (" + item.getPatientUhid() + ")");
                }
                if (holder.tvItemTime != null) {
                    String dateStr = DateFormat.format("dd MMM yyyy, hh:mm a", new Date(item.getTimestamp())).toString();
                    holder.tvItemTime.setText(dateStr);
                }
                if (holder.tvItemVerdict != null) {
                    holder.tvItemVerdict.setText(item.getTriageVerdict());
                }
                if (holder.tvItemStatus != null) {
                    holder.tvItemStatus.setText(item.getSyncStatus());
                }
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemName, tvItemTime, tvItemVerdict, tvItemStatus;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvItemTime = itemView.findViewById(R.id.tvItemTime);
                tvItemVerdict = itemView.findViewById(R.id.tvItemVerdict);
                tvItemStatus = itemView.findViewById(R.id.tvItemStatus);
            }
        }
    }
}