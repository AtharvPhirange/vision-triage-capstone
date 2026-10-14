package com.example.myapplication;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.db.ScanRecord;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScanRecordAdapter extends RecyclerView.Adapter<ScanRecordAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ScanRecord record);
        void onDeleteClick(ScanRecord record);
    }

    private final List<ScanRecord> recordList;
    private final OnItemClickListener listener;

    public ScanRecordAdapter(List<ScanRecord> recordList, OnItemClickListener listener) {
        this.recordList = recordList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scan_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanRecord record = recordList.get(position);

        if (holder.tvResult != null) {
            holder.tvResult.setText(record.getTriageResult());
        }

        if (holder.tvTimestamp != null) {
            String dateStr = DateFormat.format("dd MMM yyyy, hh:mm a", new Date(record.getScanTimestamp())).toString();
            holder.tvTimestamp.setText(dateStr);
        }

        if (holder.tvBaseline != null) {
            holder.tvBaseline.setText(String.format(Locale.getDefault(), "%.2f", record.getBaselinePupilSize()));
        }

        if (holder.tvMinRatio != null) {
            holder.tvMinRatio.setText(String.format(Locale.getDefault(), "%.2f", record.getMinimumPupilSize()));
        }

        if (holder.tvLatency != null) {
            holder.tvLatency.setText(record.getConstrictionLatencyMs() + "ms");
        }

        // Entire card click opens the graph dialog
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(record);
            }
        });

        // Dedicated PDF button trigger
        if (holder.tvPdfBtn != null) {
            holder.tvPdfBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(record);
                }
            });
        }

        // Dedicated Trash / Delete button
        if (holder.btnDeleteScan != null) {
            holder.btnDeleteScan.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(record);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return recordList != null ? recordList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvResult, tvPdfBtn, tvTimestamp, tvBaseline, tvMinRatio, tvLatency;
        ImageView btnDeleteScan;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvResult = itemView.findViewById(R.id.tvResult);
            tvPdfBtn = itemView.findViewById(R.id.tvPdfBtn);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvBaseline = itemView.findViewById(R.id.tvBaseline);
            tvMinRatio = itemView.findViewById(R.id.tvMinRatio);
            tvLatency = itemView.findViewById(R.id.tvLatency);
            btnDeleteScan = itemView.findViewById(R.id.btnDeleteScan);
        }
    }
}