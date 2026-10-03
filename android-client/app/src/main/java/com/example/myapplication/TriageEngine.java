package com.example.myapplication;

public class TriageEngine {
    public float baselineRatio = 0.0f;
    public float minConstrictedRatio = 1.0f;
    private boolean isTesting = false;
    public int validFrameCount = 0;

    private long latencyMs = 0;
    private String triageResult = "NORMAL";

    public void startTest(float baselineRatio) {
        this.baselineRatio = baselineRatio;
        this.minConstrictedRatio = baselineRatio;
        this.validFrameCount = 0; // Reset frame counter for a new scan
        this.isTesting = true;
    }

    public void processNewRatio(float ratio) {
        if (isTesting && ratio > 0.0f) {
            validFrameCount++; // Increment counter only when a valid face/eye is detected
            if (ratio < minConstrictedRatio) {
                minConstrictedRatio = ratio;
            }
        }
    }

    public float getConstrictionPercentage() {
        if (baselineRatio <= 0.0f) {
            return 0.0f;
        }
        return ((baselineRatio - minConstrictedRatio) / baselineRatio) * 100f;
    }

    public String getFinalDiagnosis(float baselineRatio, float minimumRatio) {
        if (baselineRatio <= 0) {
            this.triageResult = "ERROR";
            return this.triageResult;
        }

        // Lowered threshold to 40 valid frames to accommodate the 3-second scan
        if (validFrameCount < 40) {
            this.triageResult = "ERROR_INCOMPLETE";
            return this.triageResult;
        }

        float dropPercentage = ((baselineRatio - minimumRatio) / baselineRatio) * 100;

        // Update the internal variable so getTriageResult() returns the correct value
        if (dropPercentage >= 30) {
            this.triageResult = "NORMAL";
        } else if (dropPercentage >= 10) {
            this.triageResult = "SLUGGISH";
        } else {
            this.triageResult = "UNRESPONSIVE";
        }

        return this.triageResult;
    }

    public float getBaselineRatio() {
        return baselineRatio;
    }

    public float getMinRatio() {
        return minConstrictedRatio;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public String getTriageResult() {
        return triageResult;
    }
}