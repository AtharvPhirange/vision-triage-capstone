package com.example.myapplication; // Make sure this matches your actual package name!

import android.util.Log;

public class TriageEngine {

    private float baselineRatio = 0.0f;
    private long flashStartTime = 0;
    private boolean isTesting = false;

    // Call this exactly when you trigger the screen brightness
    public void startTest(float currentRatio) {
        this.baselineRatio = currentRatio;
        this.flashStartTime = System.currentTimeMillis();
        this.isTesting = true;

        Log.d("TriageEngine", "Flash triggered! Baseline ratio locked at: " + baselineRatio);
    }

    // Call this every time VisionBrain calculates a new ratio
    public void processNewRatio(float newRatio) {
        if (!isTesting) return;

        // Clinical threshold: If the ratio drops by at least 8% from the baseline, the pupil has constricted
        float constrictionThreshold = baselineRatio * 0.92f;

        if (newRatio <= constrictionThreshold) {
            long constrictionTime = System.currentTimeMillis();
            long latency = constrictionTime - flashStartTime;

            Log.d("TriageEngine", "REFLEX DETECTED! Pupil constricted to: " + newRatio);
            Log.d("TriageEngine", "Reaction Latency: " + latency + " milliseconds");

            // End the current test so it doesn't keep triggering
            isTesting = false;
        }
    }
}