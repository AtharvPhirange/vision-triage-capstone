package com.vision.service;

import com.vision.dto.DilationPointDto;
import com.vision.dto.ScanPayloadDto;
import com.vision.dto.ScanResponseDto;
import com.vision.model.ScanRecord;

import java.util.List;

public interface ScanService {

    ScanResponseDto submitScan(ScanPayloadDto payload);

    List<ScanRecord> getHistory(String deviceId);

    void relayLiveFrame(String deviceId, DilationPointDto framePoint);
}
