package com.example.myapplication.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scan_records")
public class ScanRecord {

    @PrimaryKey
    @NonNull
    private String scanId;
    private String patientName;
    private long scanTimestamp;
    private double baselinePupilSize;
    private double minimumPupilSize;
    private long constrictionLatencyMs;
    private String triageResult;
    private boolean isSynced;

    public ScanRecord(@NonNull String scanId, String patientName, long scanTimestamp,
                      double baselinePupilSize, double minimumPupilSize,
                      long constrictionLatencyMs, String triageResult, boolean isSynced) {
        this.scanId = scanId;
        this.patientName = patientName;
        this.scanTimestamp = scanTimestamp;
        this.baselinePupilSize = baselinePupilSize;
        this.minimumPupilSize = minimumPupilSize;
        this.constrictionLatencyMs = constrictionLatencyMs;
        this.triageResult = triageResult;
        this.isSynced = isSynced;
    }

    @NonNull
    public String getScanId() { return scanId; }
    public void setScanId(@NonNull String scanId) { this.scanId = scanId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public long getScanTimestamp() { return scanTimestamp; }
    public void setScanTimestamp(long scanTimestamp) { this.scanTimestamp = scanTimestamp; }

    public double getBaselinePupilSize() { return baselinePupilSize; }
    public void setBaselinePupilSize(double baselinePupilSize) { this.baselinePupilSize = baselinePupilSize; }

    public double getMinimumPupilSize() { return minimumPupilSize; }
    public void setMinimumPupilSize(double minimumPupilSize) { this.minimumPupilSize = minimumPupilSize; }

    public long getConstrictionLatencyMs() { return constrictionLatencyMs; }
    public void setConstrictionLatencyMs(long constrictionLatencyMs) { this.constrictionLatencyMs = constrictionLatencyMs; }

    public String getTriageResult() { return triageResult; }
    public void setTriageResult(String triageResult) { this.triageResult = triageResult; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { this.isSynced = synced; }
}