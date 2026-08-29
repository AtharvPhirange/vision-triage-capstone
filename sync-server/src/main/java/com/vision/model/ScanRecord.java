package com.vision.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity
@Table(name = "scan_records")
public class ScanRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "verdict")
    private String verdict;

    @Column(name = "min_diameter_mm")
    private double minDiameterMm;

    @Column(name = "max_diameter_mm")
    private double maxDiameterMm;

    @Column(name = "constriction_latency_ms")
    private long constrictionLatencyMs;

    @Column(name = "captured_at")
    private Instant capturedAt;

    @Column(name = "synced_at")
    private Instant syncedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dilation_curve_json", columnDefinition = "jsonb")
    private String dilationCurveJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

    public double getMinDiameterMm() { return minDiameterMm; }
    public void setMinDiameterMm(double minDiameterMm) { this.minDiameterMm = minDiameterMm; }

    public double getMaxDiameterMm() { return maxDiameterMm; }
    public void setMaxDiameterMm(double maxDiameterMm) { this.maxDiameterMm = maxDiameterMm; }

    public long getConstrictionLatencyMs() { return constrictionLatencyMs; }
    public void setConstrictionLatencyMs(long constrictionLatencyMs) { this.constrictionLatencyMs = constrictionLatencyMs; }

    public Instant getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }

    public Instant getSyncedAt() { return syncedAt; }
    public void setSyncedAt(Instant syncedAt) { this.syncedAt = syncedAt; }

    public String getDilationCurveJson() { return dilationCurveJson; }
    public void setDilationCurveJson(String dilationCurveJson) { this.dilationCurveJson = dilationCurveJson; }
}