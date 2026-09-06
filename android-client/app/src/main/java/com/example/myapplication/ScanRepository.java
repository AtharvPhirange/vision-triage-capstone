package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScanRepository {
    private static ScanRepository instance;
    private List<ScanRecord> scanRecords;

    private ScanRepository() {
        scanRecords = new ArrayList<>();
        loadInitialMockData();
    }

    public static synchronized ScanRepository getInstance() {
        if (instance == null) {
            instance = new ScanRepository();
        }
        return instance;
    }

    public List<ScanRecord> getScanRecords() {
        return scanRecords;
    }

    public void addScan(ScanRecord record) {
        scanRecords.add(0, record);
    }

    private void loadInitialMockData() {
        long currentTime = System.currentTimeMillis();

        scanRecords.add(new ScanRecord(UUID.randomUUID().toString(), "John Doe", currentTime - 3600000L, 4.8, 2.5, 220L, "NORMAL", true));
        scanRecords.add(new ScanRecord(UUID.randomUUID().toString(), "Jane Smith", currentTime - 7200000L, 5.2, 4.6, 490L, "ABNORMAL", false));
        scanRecords.add(new ScanRecord(UUID.randomUUID().toString(), "Alex Johnson", currentTime - 86400000L, 4.5, 2.2, 205L, "NORMAL", true));
        scanRecords.add(new ScanRecord(UUID.randomUUID().toString(), "Sam Wilson", currentTime - 172800000L, 5.0, 4.7, 530L, "ABNORMAL", true));
    }
}