package com.vision.controller;

import com.vision.dto.ScanPayloadDto;
import com.vision.dto.ScanResponseDto;
import com.vision.model.ScanRecord;
import com.vision.service.ScanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scans")
public class SyncController {

    private final ScanService scanService;

    public SyncController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping
    public ResponseEntity<ScanResponseDto> submitScan(@RequestBody ScanPayloadDto payload) {
        ScanResponseDto response = scanService.submitScan(payload);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<List<ScanRecord>> getHistory(@PathVariable String deviceId) {
        List<ScanRecord> history = scanService.getHistory(deviceId);
        return ResponseEntity.ok(history);
    }
}

