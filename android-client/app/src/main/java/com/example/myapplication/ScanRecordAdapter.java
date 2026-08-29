package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.db.ScanRecord;
import java.util.ArrayList;
import java.util.List;

public class ScanRecordAdapter extends RecyclerView.Adapter<ScanRecordAdapter.ScanViewHolder> {

    private List<ScanRecord> scanList = new ArrayList<>();

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scan_record, parent, false);
        return new ScanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        ScanRecord record = scanList.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return scanList.size();
    }

    public void setScanList(List<ScanRecord> scanList) {
        this.scanList = scanList;
        notifyDataSetChanged();
    }

    static class ScanViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvSyncStatus, tvTriageResult, tvScanMetrics;

        public ScanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvSyncStatus = itemView.findViewById(R.id.tvSyncStatus);
            tvTriageResult = itemView.findViewById(R.id.tvTriageResult);
            tvScanMetrics = itemView.findViewById(R.id.tvScanMetrics);
        }

        public void bind(ScanRecord record) {
            tvPatientName.setText(record.getPatientName());
            tvTriageResult.setText("Result: " + record.getTriageResult());

            tvScanMetrics.setText(String.format("Base: %.2f | Min: %.2f | Latency: %dms",
                    record.getBaselinePupilSize(),
                    record.getMinimumPupilSize(),
                    record.getConstrictionLatencyMs()));

            if (record.isSynced()) {
                tvSyncStatus.setText("● SYNCED");
                tvSyncStatus.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                tvSyncStatus.setText("○ PENDING");
                tvSyncStatus.setTextColor(Color.parseColor("#E65100"));
            }
        }
    }
}