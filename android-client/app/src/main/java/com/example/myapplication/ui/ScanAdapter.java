package com.example.myapplication.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ScanRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ScanViewHolder> {

    private List<ScanRecord> scanRecords;
    private OnScanClickListener listener;

    public interface OnScanClickListener {
        void onScanClick(ScanRecord record);
    }

    public ScanAdapter(List<ScanRecord> scanRecords, OnScanClickListener listener) {
        this.scanRecords = scanRecords;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan_record, parent, false);
        return new ScanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        ScanRecord record = scanRecords.get(position);
        holder.bind(record, listener);
    }

    @Override
    public int getItemCount() {
        return scanRecords != null ? scanRecords.size() : 0;
    }

    public void updateData(List<ScanRecord> newRecords) {
        this.scanRecords = newRecords;
        notifyDataSetChanged();
    }

    static class ScanViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPatientName;
        private final TextView tvScanTimestamp;
        private final TextView tvTriageResult;

        public ScanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvScanTimestamp = itemView.findViewById(R.id.tvScanTimestamp);
            tvTriageResult = itemView.findViewById(R.id.tvTriageResult);
        }

        public void bind(ScanRecord record, OnScanClickListener listener) {
            tvPatientName.setText(record.getPatientName());

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault());
            tvScanTimestamp.setText(dateFormat.format(new Date(record.getScanTimestamp())));

            tvTriageResult.setText(record.getTriageResult());
            if ("ABNORMAL".equalsIgnoreCase(record.getTriageResult())) {
                tvTriageResult.setTextColor(Color.RED);
            } else {
                tvTriageResult.setTextColor(Color.GREEN);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onScanClick(record);
                }
            });
        }
    }
}