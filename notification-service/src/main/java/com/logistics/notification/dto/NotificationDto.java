package com.logistics.notification.dto;

import java.time.LocalDateTime;

public class NotificationDto {
    private Long id;
    private Long userId;
    private Long shipmentId;
    private String trackingNumber;
    private String message;
    private String type;
    private Boolean readStatus;
    private LocalDateTime createdAt;

    public NotificationDto() {}

    public NotificationDto(Long id, Long userId, Long shipmentId, String trackingNumber, String message, String type, Boolean readStatus, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.message = message;
        this.type = type;
        this.readStatus = readStatus;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getReadStatus() { return readStatus; }
    public void setReadStatus(Boolean readStatus) { this.readStatus = readStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
