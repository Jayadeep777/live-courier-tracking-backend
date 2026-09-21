package com.logistics.shipment.client;

import com.logistics.shipment.dto.TrackingEventDto;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class TrackingClientFallbackFactory implements FallbackFactory<TrackingClient> {
    @Override
    public TrackingClient create(Throwable cause) {
        return new TrackingClient() {
            @Override
            public TrackingEventDto createEvent(Map<String, Object> payload) {
                return null; // Graceful fallback if tracking service is down
            }

            @Override
            public List<TrackingEventDto> getHistory(String trackingNumber) {
                return Collections.emptyList();
            }
        };
    }
}
