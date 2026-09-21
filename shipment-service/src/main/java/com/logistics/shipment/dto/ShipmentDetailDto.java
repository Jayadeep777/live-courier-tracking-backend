package com.logistics.shipment.dto;

import java.util.List;

public class ShipmentDetailDto {
    private ShipmentDto shipment;
    private List<TrackingEventDto> trackingHistory;

    public ShipmentDetailDto() {}

    public ShipmentDetailDto(ShipmentDto shipment, List<TrackingEventDto> trackingHistory) {
        this.shipment = shipment;
        this.trackingHistory = trackingHistory;
    }

    public ShipmentDto getShipment() { return shipment; }
    public void setShipment(ShipmentDto shipment) { this.shipment = shipment; }

    public List<TrackingEventDto> getTrackingHistory() { return trackingHistory; }
    public void setTrackingHistory(List<TrackingEventDto> trackingHistory) { this.trackingHistory = trackingHistory; }
}
