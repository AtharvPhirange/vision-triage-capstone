package com.vision.controller;

import com.vision.dto.DilationPointDto;
import com.vision.service.ScanService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class SupervisorSocketHandler {

    private final ScanService scanService;

    public SupervisorSocketHandler(ScanService scanService) {
        this.scanService = scanService;
    }

    @MessageMapping("/supervise/{deviceId}")
    public void receiveLiveFrame(@DestinationVariable String deviceId, @Payload DilationPointDto framePoint) {
        scanService.relayLiveFrame(deviceId, framePoint);
    }
}

