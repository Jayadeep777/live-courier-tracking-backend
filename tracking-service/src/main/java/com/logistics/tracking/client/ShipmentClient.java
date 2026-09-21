package com.logistics.tracking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "SHIPMENT-SERVICE", fallbackFactory = ShipmentClientFallbackFactory.class)
public interface ShipmentClient {

    @GetMapping("/api/shipments/{id}")
    Map<String, Object> getShipmentById(@PathVariable("id") Long id);

    @PutMapping("/api/shipments/{id}")
    Map<String, Object> updateShipment(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);
}
