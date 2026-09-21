package com.logistics.tracking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_events")
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "tracking_number", nullable = false, length = 50)
    private String trackingNumber;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 255)
    private String description;

    @Column(updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public TrackingEvent() {}

    public TrackingEvent(Long shipmentId, String trackingNumber, String location, String status, String description) {
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.location = location;
        this.status = status;
        this.description = description;
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
