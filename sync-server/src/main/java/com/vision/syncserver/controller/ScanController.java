package com.vision.syncserver.controller;

import com.vision.syncserver.model.ScanRecordEntity;
import com.vision.syncserver.repository.ScanRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scans")
public class ScanController {

    private final ScanRecordRepository repository;

    public ScanController(ScanRecordRepository repository) {
        this.repository = repository;
    }

    // Sync payload endpoint hit by Android's SyncWorker
    @PostMapping("/sync")
    public ResponseEntity<ScanRecordEntity> syncScan(@RequestBody ScanRecordEntity scanRecord) {
        scanRecord.setIsSynced(true);
        ScanRecordEntity savedRecord = repository.save(scanRecord);
        return ResponseEntity.ok(savedRecord);
    }

    // Get all synced scans
    @GetMapping
    public ResponseEntity<List<ScanRecordEntity>> getAllScans() {
        return ResponseEntity.ok(repository.findAll());
    }

    // Get scan history for a specific patient
    @GetMapping("/history/{patientName}")
    public ResponseEntity<List<ScanRecordEntity>> getPatientHistory(@PathVariable String patientName) {
        List<ScanRecordEntity> patientRecords = repository.findByPatientName(patientName);
        return ResponseEntity.ok(patientRecords);
    }
}