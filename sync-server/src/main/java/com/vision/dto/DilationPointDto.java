package com.vision.dto;

public class DilationPointDto {

    private long timestampMs;
    private double diameterMm;

    public long getTimestampMs() {
        return timestampMs;
    }

    public void setTimestampMs(long timestampMs) {
        this.timestampMs = timestampMs;
    }

    public double getDiameterMm() {
        return diameterMm;
    }

    public void setDiameterMm(double diameterMm) {
        this.diameterMm = diameterMm;
    }
}

