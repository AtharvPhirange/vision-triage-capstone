package com.example.myapplication;

public class TriageEngine {
    public float baselineRatio = 0.0f;
    public float minConstrictedRatio = 1.0f;
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

    public String getFinalDiagnosis(float baselineRatio, float minimumRatio) {
        if (baselineRatio <= 0) {
            return "ERROR";
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