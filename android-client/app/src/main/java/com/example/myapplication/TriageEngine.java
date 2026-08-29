package com.example.myapplication;

public class TriageEngine {
    public float baselineRatio = 0.0f;
    public float minConstrictedRatio = 1.0f;
    private boolean isTesting = false;
    public int validFrameCount = 0;

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
            return "ERROR";
        }

        // Require at least 150 valid frames (approx 5 seconds of clean data) to prevent false diagnoses
        if (validFrameCount < 150) {
            return "ERROR_INCOMPLETE";
        }

        float dropPercentage = ((baselineRatio - minimumRatio) / baselineRatio) * 100;

        if (dropPercentage >= 30) {
            return "NORMAL";
        } else if (dropPercentage >= 10) {
            return "SLUGGISH";
        } else {
            return "UNRESPONSIVE";
        }
    }
}