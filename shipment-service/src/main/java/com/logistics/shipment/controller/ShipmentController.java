package com.logistics.shipment.controller;

import com.logistics.shipment.dto.*;
import com.logistics.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<ShipmentDto> createShipment(
            @Valid @RequestBody CreateShipmentRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId) {
        ShipmentDto created = shipmentService.createShipment(request, userId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDto>> getShipments(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(shipmentService.getAllShipments());
        }
        if ("COURIER".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(shipmentService.getCourierShipments(userId, null));
        }
        if (userId != null) {
            return ResponseEntity.ok(shipmentService.getUserShipments(userId));
        }
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<ShipmentDto>> getAssignedShipments(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(shipmentService.getCourierShipments(userId, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDto> getShipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getShipmentById(id));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ShipmentDetailDto> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(shipmentService.getShipmentDetailByTrackingNumber(trackingNumber));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShipmentDto> updateShipment(
            @PathVariable Long id,
            @RequestBody UpdateShipmentRequest request) {
        return ResponseEntity.ok(shipmentService.updateShipment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }
}
