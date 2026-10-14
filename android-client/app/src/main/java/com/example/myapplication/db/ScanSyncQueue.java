package com.example.myapplication.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scan_sync_queue")
public class ScanSyncQueue {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String patientUhid;
    private String patientName;
    private String symptomsJson;
    private String metricsJson;
    private String timeSeriesJson;
    private String triageVerdict;
    private long timestamp;
    private String syncStatus; // "PENDING_SYNC", "SYNCED", "FAILED"

    public ScanSyncQueue(String patientUhid, String patientName, String symptomsJson, String metricsJson,
                         String timeSeriesJson, String triageVerdict, long timestamp, String syncStatus) {
        this.patientUhid = patientUhid;
        this.patientName = patientName;
        this.symptomsJson = symptomsJson;
        this.metricsJson = metricsJson;
        this.timeSeriesJson = timeSeriesJson;
        this.triageVerdict = triageVerdict;
        this.timestamp = timestamp;
        this.syncStatus = syncStatus;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getPatientUhid() { return patientUhid; }
    public String getPatientName() { return patientName; }
    public String getSymptomsJson() { return symptomsJson; }
    public String getMetricsJson() { return metricsJson; }
    public String getTimeSeriesJson() { return timeSeriesJson; }
    public String getTriageVerdict() { return triageVerdict; }
    public long getTimestamp() { return timestamp; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}