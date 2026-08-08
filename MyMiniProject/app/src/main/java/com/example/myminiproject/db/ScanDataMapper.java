package com.example.myminiproject.db;

import java.util.UUID;

public class ScanDataMapper {

    public static ScanRecord createRecord(
            String patientName,
            double baselinePupilSize,
            double minPupilSize,
            long latencyMs,
            String triageResult
    ) {
        return new ScanRecord(
                UUID.randomUUID().toString(), // Generates a dynamic unique scan ID
                patientName,
                System.currentTimeMillis(),    // Current timestamp
                baselinePupilSize,
                minPupilSize,
                latencyMs,
                triageResult,
                false                          // isSynced = false for WorkManager
        );
    }
}