package com.logistics.tracking.dto;

import java.time.LocalDateTime;

public class TrackingEventDto {
    private Long id;
    private Long shipmentId;
    private String trackingNumber;
    private String location;
    private String status;
    private String description;
    private LocalDateTime timestamp;

    public TrackingEventDto() {}

    public TrackingEventDto(Long id, Long shipmentId, String trackingNumber, String location, String status, String description, LocalDateTime timestamp) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.location = location;
        this.status = status;
        this.description = description;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
