package com.vision.dto;

import java.time.Instant;
import java.util.List;

public class ScanPayloadDto {

    private String deviceId;
    private String verdict;
    private double minDiameterMm;
    private double maxDiameterMm;
    private long constrictionLatencyMs;
    private Instant capturedAt;
    private List<DilationPointDto> dilationCurve;

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

    public List<DilationPointDto> getDilationCurve() {
        return dilationCurve;
    }

    public void setDilationCurve(List<DilationPointDto> dilationCurve) {
        this.dilationCurve = dilationCurve;
    }
}

