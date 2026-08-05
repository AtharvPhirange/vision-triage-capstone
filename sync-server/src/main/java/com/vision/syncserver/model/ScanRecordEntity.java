package com.vision.syncserver.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "scan_records")
public class ScanRecordEntity {

    @Id
    private String scanId;
    private String patientName;
    private Long scanTimestamp;
    private Double baselinePupilSize;
    private Double minimumPupilSize;
    private Long constrictionLatencyMs;
    private String triageResult;
    private Boolean isSynced;

    public ScanRecordEntity() {
    }

    public ScanRecordEntity(String scanId, String patientName, Long scanTimestamp, Double baselinePupilSize, Double minimumPupilSize, Long constrictionLatencyMs, String triageResult, Boolean isSynced) {
        this.scanId = scanId;
        this.patientName = patientName;
        this.scanTimestamp = scanTimestamp;
        this.baselinePupilSize = baselinePupilSize;
        this.minimumPupilSize = minimumPupilSize;
        this.constrictionLatencyMs = constrictionLatencyMs;
        this.triageResult = triageResult;
        this.isSynced = isSynced;
    }

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getScanTimestamp() {
        return scanTimestamp;
    }

    public void setScanTimestamp(Long scanTimestamp) {
        this.scanTimestamp = scanTimestamp;
    }

    public Double getBaselinePupilSize() {
        return baselinePupilSize;
    }

    public void setBaselinePupilSize(Double baselinePupilSize) {
        this.baselinePupilSize = baselinePupilSize;
    }

    public Double getMinimumPupilSize() {
        return minimumPupilSize;
    }

    public void setMinimumPupilSize(Double minimumPupilSize) {
        this.minimumPupilSize = minimumPupilSize;
    }

    public Long getConstrictionLatencyMs() {
        return constrictionLatencyMs;
    }

    public void setConstrictionLatencyMs(Long constrictionLatencyMs) {
        this.constrictionLatencyMs = constrictionLatencyMs;
    }

    public String getTriageResult() {
        return triageResult;
    }

    public void setTriageResult(String triageResult) {
        this.triageResult = triageResult;
    }

    public Boolean getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(Boolean isSynced) {
        this.isSynced = isSynced;
    }
}