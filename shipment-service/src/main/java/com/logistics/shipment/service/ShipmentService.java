package com.logistics.shipment.service;

import com.logistics.shipment.client.NotificationClient;
import com.logistics.shipment.client.TrackingClient;
import com.logistics.shipment.dto.*;
import com.logistics.shipment.model.Shipment;
import com.logistics.shipment.model.ShipmentStatus;
import com.logistics.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final TrackingClient trackingClient;
    private final NotificationClient notificationClient;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           TrackingClient trackingClient,
                           NotificationClient notificationClient) {
        this.shipmentRepository = shipmentRepository;
        this.trackingClient = trackingClient;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public ShipmentDto createShipment(CreateShipmentRequest request, Long userId) {
        String trackingNumber = generateTrackingNumber();

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(trackingNumber);
        shipment.setSenderName(request.getSenderName());
        shipment.setSenderAddress(request.getSenderAddress());
        shipment.setReceiverName(request.getReceiverName());
        shipment.setReceiverAddress(request.getReceiverAddress());
        shipment.setPackageDescription(request.getPackageDescription());
        shipment.setPackageWeight(request.getPackageWeight());
        shipment.setCurrentLocation(request.getCurrentLocation());
        shipment.setStatus(ShipmentStatus.BOOKED);
        shipment.setEstimatedDelivery(request.getEstimatedDelivery() != null ? 
                request.getEstimatedDelivery() : LocalDate.now().plusDays(3).toString());
        shipment.setUserId(userId);

        Shipment saved = shipmentRepository.save(shipment);

        // 1. Trigger Tracking Event Creation via Feign
        try {
            Map<String, Object> eventPayload = new HashMap<>();
            eventPayload.put("shipmentId", saved.getId());
            eventPayload.put("trackingNumber", saved.getTrackingNumber());
            eventPayload.put("location", saved.getCurrentLocation());
            eventPayload.put("status", saved.getStatus().name());
            eventPayload.put("description", "Shipment created & initial dispatch booked.");
            trackingClient.createEvent(eventPayload);
        } catch (Exception ignored) {}

        // 2. Trigger Notification via Feign
        try {
            CreateNotificationRequest notifRequest = new CreateNotificationRequest(
                    userId,
                    saved.getId(),
                    saved.getTrackingNumber(),
                    "Shipment " + saved.getTrackingNumber() + " created successfully.",
                    "SUCCESS"
            );
            notificationClient.sendNotification(notifRequest);
        } catch (Exception ignored) {}

        return mapToDto(saved);
    }

    public List<ShipmentDto> getUserShipments(Long userId) {
        return shipmentRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ShipmentDto> getCourierShipments(Long courierId, String courierName) {
        if (courierId == null && (courierName == null || courierName.isBlank())) {
            return List.of();
        }
        return shipmentRepository.findAll().stream()
                .filter(s -> (courierId != null && courierId.equals(s.getCourierId())) ||
                             (courierName != null && !courierName.isBlank() && s.getCourierName() != null && 
                              s.getCourierName().toLowerCase().contains(courierName.toLowerCase())))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ShipmentDto> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ShipmentDto getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found with id: " + id));
        return mapToDto(shipment);
    }

    public ShipmentDetailDto getShipmentDetailByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipment not found with tracking number: " + trackingNumber));

        List<TrackingEventDto> history;
        try {
            history = trackingClient.getHistory(trackingNumber);
        } catch (Exception e) {
            history = List.of();
        }

        return new ShipmentDetailDto(mapToDto(shipment), history);
    }

    @Transactional
    public ShipmentDto updateShipment(Long id, UpdateShipmentRequest request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found with id: " + id));

        if (request.getStatus() != null) {
            shipment.setStatus(request.getStatus());
        }
        if (request.getCurrentLocation() != null && !request.getCurrentLocation().isBlank()) {
            shipment.setCurrentLocation(request.getCurrentLocation());
        }
        if (request.getEstimatedDelivery() != null && !request.getEstimatedDelivery().isBlank()) {
            shipment.setEstimatedDelivery(request.getEstimatedDelivery());
        }
        if (request.getCourierId() != null) {
            shipment.setCourierId(request.getCourierId());
        }
        if (request.getCourierName() != null && !request.getCourierName().isBlank()) {
            shipment.setCourierName(request.getCourierName());
        }

        Shipment updated = shipmentRepository.save(shipment);

        // Record tracking event
        try {
            Map<String, Object> eventPayload = new HashMap<>();
            eventPayload.put("shipmentId", updated.getId());
            eventPayload.put("trackingNumber", updated.getTrackingNumber());
            eventPayload.put("location", updated.getCurrentLocation());
            eventPayload.put("status", updated.getStatus().name());
            eventPayload.put("description", request.getDescription() != null && !request.getDescription().isBlank() ? 
                    request.getDescription() : "Status updated to " + updated.getStatus());
            trackingClient.createEvent(eventPayload);
        } catch (Exception ignored) {}

        // Send notification to customer
        try {
            CreateNotificationRequest notifRequest = new CreateNotificationRequest(
                    updated.getUserId(),
                    updated.getId(),
                    updated.getTrackingNumber(),
                    "Shipment " + updated.getTrackingNumber() + " is now " + updated.getStatus() + " at " + updated.getCurrentLocation(),
                    "INFO"
            );
            notificationClient.sendNotification(notifRequest);
        } catch (Exception ignored) {}

        // Send notification to assigned courier partner if newly assigned
        if (request.getCourierId() != null) {
            try {
                CreateNotificationRequest courierNotif = new CreateNotificationRequest(
                        request.getCourierId(),
                        updated.getId(),
                        updated.getTrackingNumber(),
                        "Work Assignment: Parcel " + updated.getTrackingNumber() + " has been assigned to you for delivery.",
                        "INFO"
                );
                notificationClient.sendNotification(courierNotif);
            } catch (Exception ignored) {}
        }

        return mapToDto(updated);
    }

    @Transactional
    public void deleteShipment(Long id) {
        if (!shipmentRepository.existsById(id)) {
            throw new RuntimeException("Shipment not found with id: " + id);
        }
        shipmentRepository.deleteById(id);
    }

    private String generateTrackingNumber() {
        return "TRK-" + (10000 + new Random().nextInt(90000));
    }

    private ShipmentDto mapToDto(Shipment s) {
        ShipmentDto dto = new ShipmentDto();
        dto.setId(s.getId());
        dto.setTrackingNumber(s.getTrackingNumber());
        dto.setSenderName(s.getSenderName());
        dto.setSenderAddress(s.getSenderAddress());
        dto.setReceiverName(s.getReceiverName());
        dto.setReceiverAddress(s.getReceiverAddress());
        dto.setPackageDescription(s.getPackageDescription());
        dto.setPackageWeight(s.getPackageWeight());
        dto.setStatus(s.getStatus());
        dto.setCurrentLocation(s.getCurrentLocation());
        dto.setEstimatedDelivery(s.getEstimatedDelivery());
        dto.setUserId(s.getUserId());
        dto.setCourierId(s.getCourierId());
        dto.setCourierName(s.getCourierName());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setUpdatedAt(s.getUpdatedAt());
        return dto;
    }
}
