package com.logistics.tracking.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class ShipmentClientFallbackFactory implements FallbackFactory<ShipmentClient> {
    @Override
    public ShipmentClient create(Throwable cause) {
        return new ShipmentClient() {
            @Override
            public Map<String, Object> getShipmentById(Long id) {
                return Collections.emptyMap();
            }

            @Override
            public Map<String, Object> updateShipment(Long id, Map<String, Object> request) {
                return Collections.emptyMap(); // Graceful fallback
            }
        };
    }
}
