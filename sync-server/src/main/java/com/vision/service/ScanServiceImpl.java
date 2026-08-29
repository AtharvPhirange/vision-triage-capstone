package com.vision.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vision.dto.DilationPointDto;
import com.vision.dto.ScanPayloadDto;
import com.vision.dto.ScanResponseDto;
import com.vision.model.ScanRecord;
import com.vision.repository.ScanRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ScanServiceImpl implements ScanService {

    private final ScanRepository scanRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public ScanServiceImpl(ScanRepository scanRepository, ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate) {
        this.scanRepository = scanRepository;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public ScanResponseDto submitScan(ScanPayloadDto payload) {
        ScanRecord record = new ScanRecord();
        record.setDeviceId(payload.getDeviceId());
        record.setVerdict(payload.getVerdict());
        record.setMinDiameterMm(payload.getMinDiameterMm());
        record.setMaxDiameterMm(payload.getMaxDiameterMm());
        record.setConstrictionLatencyMs(payload.getConstrictionLatencyMs());
        record.setCapturedAt(payload.getCapturedAt());
        record.setSyncedAt(Instant.now());
        record.setDilationCurveJson(toJson(payload.getDilationCurve()));

        ScanRecord saved = scanRepository.save(record);

        return new ScanResponseDto(saved.getId(), "SYNCED", "Scan stored successfully");
    }

    @Override
    public List<ScanRecord> getHistory(String deviceId) {
        return scanRepository.findByDeviceIdOrderByCapturedAtDesc(deviceId);
    }

    @Override
    public void relayLiveFrame(String deviceId, DilationPointDto framePoint) {
        messagingTemplate.convertAndSend("/topic/supervise/" + deviceId, framePoint);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize dilation curve", e);
        }
    }
}
