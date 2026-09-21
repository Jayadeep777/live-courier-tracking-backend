package com.logistics.shipment.client;

import com.logistics.shipment.dto.TrackingEventDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "TRACKING-SERVICE", fallbackFactory = TrackingClientFallbackFactory.class)
public interface TrackingClient {

    @PostMapping("/api/tracking/events")
    TrackingEventDto createEvent(@RequestBody Map<String, Object> payload);

    @GetMapping("/api/tracking/events/{trackingNumber}")
    List<TrackingEventDto> getHistory(@PathVariable("trackingNumber") String trackingNumber);
}
