package com.vision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "scan_records")
public class ScanRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String verdict;

    private double minDiameterMm;
    private double maxDiameterMm;
    private long constrictionLatencyMs;

    @Column(nullable = false)
    private Instant capturedAt;

    @Column(nullable = false)
    private Instant syncedAt;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String dilationCurveJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public double getMinDiameterMm() {
        return minDiameterMm;
    }

    public void setMinDiameterMm(double minDiameterMm) {
        this.minDiameterMm = minDiameterMm;
    }

    public double getMaxDiameterMm() {
        return maxDiameterMm;
    }

    public void setMaxDiameterMm(double maxDiameterMm) {
        this.maxDiameterMm = maxDiameterMm;
    }

    public long getConstrictionLatencyMs() {
        return constrictionLatencyMs;
    }

    public void setConstrictionLatencyMs(long constrictionLatencyMs) {
        this.constrictionLatencyMs = constrictionLatencyMs;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(Instant capturedAt) {
        this.capturedAt = capturedAt;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(Instant syncedAt) {
        this.syncedAt = syncedAt;
    }

    public String getDilationCurveJson() {
        return dilationCurveJson;
    }

    public void setDilationCurveJson(String dilationCurveJson) {
        this.dilationCurveJson = dilationCurveJson;
    }
}

