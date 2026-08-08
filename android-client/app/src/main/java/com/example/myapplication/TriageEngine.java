package com.example.myapplication;

public class TriageEngine {
    private float baselineRatio = 0.0f;
    private float minConstrictedRatio = 1.0f;
    private boolean isTesting = false;

    public void startTest(float baselineRatio) {
        this.baselineRatio = baselineRatio;
        this.minConstrictedRatio = baselineRatio;
        this.isTesting = true;
    }

    public void processNewRatio(float ratio) {
        if (isTesting && ratio > 0.0f) {
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
}