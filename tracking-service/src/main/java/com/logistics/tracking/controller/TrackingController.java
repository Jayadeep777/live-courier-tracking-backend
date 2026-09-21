package com.logistics.tracking.controller;

import com.logistics.tracking.dto.CreateTrackingEventRequest;
import com.logistics.tracking.dto.TrackingEventDto;
import com.logistics.tracking.service.TrackingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/events")
    public ResponseEntity<TrackingEventDto> createEvent(@RequestBody CreateTrackingEventRequest request) {
        TrackingEventDto created = trackingService.createEvent(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/events/{trackingNumber}")
    public ResponseEntity<List<TrackingEventDto>> getHistory(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(trackingService.getHistoryByTrackingNumber(trackingNumber));
    }

    @PostMapping("/simulate-movement/{trackingNumber}")
    public ResponseEntity<Map<String, String>> simulateMovement(
            @PathVariable String trackingNumber,
            @RequestParam(required = false, defaultValue = "1") Long shipmentId,
            @RequestParam(required = false, defaultValue = "2") Long userId) {
        trackingService.simulateMovement(trackingNumber, shipmentId, userId);
        return ResponseEntity.ok(Map.of("message", "Simulated movement telemetry started for " + trackingNumber));
    }
}
