package com.logistics.shipment.dto;

public class CreateNotificationRequest {
    private Long userId;
    private Long shipmentId;
    private String trackingNumber;
    private String message;
    private String type;

    public CreateNotificationRequest() {}

    public CreateNotificationRequest(Long userId, Long shipmentId, String trackingNumber, String message, String type) {
        this.userId = userId;
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.message = message;
        this.type = type;
    }

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
}
