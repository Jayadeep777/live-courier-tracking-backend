package com.logistics.tracking.service;

import com.logistics.tracking.client.NotificationClient;
import com.logistics.tracking.client.ShipmentClient;
import com.logistics.tracking.dto.CreateTrackingEventRequest;
import com.logistics.tracking.dto.TrackingEventDto;
import com.logistics.tracking.model.TrackingEvent;
import com.logistics.tracking.repository.TrackingEventRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class TrackingService {

    private final TrackingEventRepository repository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationClient notificationClient;
    private final ShipmentClient shipmentClient;
    private final Executor taskExecutor;

    public TrackingService(TrackingEventRepository repository,
                           SimpMessagingTemplate messagingTemplate,
                           NotificationClient notificationClient,
                           ShipmentClient shipmentClient,
                           Executor taskExecutor) {
        this.repository = repository;
        this.messagingTemplate = messagingTemplate;
        this.notificationClient = notificationClient;
        this.shipmentClient = shipmentClient;
        this.taskExecutor = taskExecutor;
    }

    @Transactional
    public TrackingEventDto createEvent(CreateTrackingEventRequest request) {
        TrackingEvent event = new TrackingEvent(
                request.getShipmentId(),
                request.getTrackingNumber(),
                request.getLocation(),
                request.getStatus(),
                request.getDescription()
        );
        TrackingEvent saved = repository.saveAndFlush(event);
        TrackingEventDto dto = mapToDto(saved);

        // STOMP WebSocket broadcast to specific shipment subscribers & global stream
        try {
            messagingTemplate.convertAndSend("/topic/shipment/" + saved.getTrackingNumber(), dto);
            messagingTemplate.convertAndSend("/topic/shipments", dto);
        } catch (Exception ignored) {}

        return dto;
    }

    public List<TrackingEventDto> getHistoryByTrackingNumber(String trackingNumber) {
        return repository.findByTrackingNumberOrderByTimestampAsc(trackingNumber).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteOldEvents(String trackingNumber) {
        try {
            repository.deleteByTrackingNumber(trackingNumber);
        } catch (Exception ignored) {}
    }

    public void simulateMovement(String trackingNumber, Long shipmentId, Long userId) {
        taskExecutor.execute(() -> {
            deleteOldEvents(trackingNumber);

            String origin = "Rajahmundry";
            String destination = "Vijayawada";

            // Dynamically fetch shipment sender & receiver details via Feign Client
            try {
                if (shipmentId != null) {
                    Map<String, Object> shipmentData = shipmentClient.getShipmentById(shipmentId);
                    if (shipmentData != null && !shipmentData.isEmpty()) {
                        if (shipmentData.get("senderAddress") != null) {
                            origin = extractCityName((String) shipmentData.get("senderAddress"));
                        } else if (shipmentData.get("senderName") != null) {
                            origin = extractCityName((String) shipmentData.get("senderName"));
                        }
                        
                        if (shipmentData.get("receiverAddress") != null) {
                            destination = extractCityName((String) shipmentData.get("receiverAddress"));
                        } else if (shipmentData.get("receiverName") != null) {
                            destination = extractCityName((String) shipmentData.get("receiverName"));
                        }
                    }
                }
            } catch (Exception ignored) {}

            String intermediateHub = getIntermediateHubName(origin, destination);

            // Strictly 4 clean waypoints: Origin -> 1 Intermediate City -> Receiver Hub -> Destination
            String[] waypoints = {
                origin + " Dispatch Terminal",
                intermediateHub + " Transit Hub",
                destination + " Local Depot",
                destination + " Destination Address"
            };
            String[] statuses = {"PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"};

            for (int i = 0; i < waypoints.length; i++) {
                try {
                    Thread.sleep(2500); // 2.5-second step
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                CreateTrackingEventRequest req = new CreateTrackingEventRequest();
                req.setShipmentId(shipmentId != null ? shipmentId : 1L);
                req.setTrackingNumber(trackingNumber);
                req.setLocation(waypoints[i]);
                req.setStatus(statuses[i]);
                req.setDescription("Courier reached " + waypoints[i]);

                TrackingEventDto eventDto = createEvent(req);

                // Update Shipment entity in Shipment Service
                if (shipmentId != null) {
                    try {
                        Map<String, Object> updateReq = new HashMap<>();
                        updateReq.put("status", statuses[i]);
                        updateReq.put("currentLocation", waypoints[i]);
                        updateReq.put("description", "Arrived at " + waypoints[i]);
                        shipmentClient.updateShipment(shipmentId, updateReq);
                    } catch (Exception ignored) {}
                }

                // Push notification
                try {
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("userId", userId != null ? userId : 2L);
                    notif.put("shipmentId", shipmentId != null ? shipmentId : 1L);
                    notif.put("trackingNumber", trackingNumber);
                    notif.put("message", "Shipment " + trackingNumber + " is now " + statuses[i] + " at " + waypoints[i]);
                    notif.put("type", "INFO");
                    notificationClient.sendNotification(notif);
                } catch (Exception ignored) {}
            }
        });
    }

    private String extractCityName(String text) {
        if (text == null || text.isBlank()) return "Origin";
        String[] parts = text.split("[,\\s-]+");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.equalsIgnoreCase("AP") || trimmed.equalsIgnoreCase("Telangana") || trimmed.equalsIgnoreCase("India") || trimmed.length() < 3) {
                continue;
            }
            return trimmed;
        }
        return parts[0].trim();
    }

    private String getIntermediateHubName(String origin, String destination) {
        String origLower = origin.toLowerCase();
        String destLower = destination.toLowerCase();

        if ((origLower.contains("rajahmundry") && destLower.contains("vijayawada")) ||
            (origLower.contains("vijayawada") && destLower.contains("rajahmundry"))) {
            return "Eluru";
        }

        if ((origLower.contains("hyderabad") && destLower.contains("visakhapatnam")) ||
            (origLower.contains("visakhapatnam") && destLower.contains("hyderabad"))) {
            return "Vijayawada";
        }

        if ((origLower.contains("hyderabad") && destLower.contains("vijayawada")) ||
            (origLower.contains("vijayawada") && destLower.contains("hyderabad"))) {
            return "Suryapet";
        }

        if ((origLower.contains("rajahmundry") && destLower.contains("visakhapatnam")) ||
            (origLower.contains("visakhapatnam") && destLower.contains("rajahmundry"))) {
            return "Kakinada";
        }

        if ((origLower.contains("vijayawada") && destLower.contains("tirupati")) ||
            (origLower.contains("tirupati") && destLower.contains("vijayawada"))) {
            return "Nellore";
        }

        return "Transit Hub";
    }

    private TrackingEventDto mapToDto(TrackingEvent e) {
        return new TrackingEventDto(
                e.getId(),
                e.getShipmentId(),
                e.getTrackingNumber(),
                e.getLocation(),
                e.getStatus(),
                e.getDescription(),
                e.getTimestamp()
        );
    }
}
