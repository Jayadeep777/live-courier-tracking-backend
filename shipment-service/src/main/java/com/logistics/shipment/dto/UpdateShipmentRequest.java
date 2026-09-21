package com.logistics.shipment.dto;

import com.logistics.shipment.model.ShipmentStatus;

public class UpdateShipmentRequest {
    private ShipmentStatus status;
    private String currentLocation;
    private String estimatedDelivery;
    private String description;
    private Long courierId;
    private String courierName;

    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public String getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(String estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCourierId() { return courierId; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }
}
