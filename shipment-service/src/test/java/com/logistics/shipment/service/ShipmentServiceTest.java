package com.logistics.shipment.service;

import com.logistics.shipment.client.NotificationClient;
import com.logistics.shipment.client.TrackingClient;
import com.logistics.shipment.dto.CreateShipmentRequest;
import com.logistics.shipment.dto.ShipmentDto;
import com.logistics.shipment.model.Shipment;
import com.logistics.shipment.model.ShipmentStatus;
import com.logistics.shipment.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private TrackingClient trackingClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private ShipmentService shipmentService;

    private Shipment sampleShipment;

    @BeforeEach
    void setUp() {
        sampleShipment = new Shipment();
        sampleShipment.setId(1L);
        sampleShipment.setTrackingNumber("TRK-10001");
        sampleShipment.setSenderName("Tech Corp");
        sampleShipment.setSenderAddress("Hyderabad");
        sampleShipment.setReceiverName("Retail Hub");
        sampleShipment.setReceiverAddress("Vijayawada");
        sampleShipment.setPackageDescription("Sensors");
        sampleShipment.setPackageWeight(3.5);
        sampleShipment.setCurrentLocation("Hyderabad");
        sampleShipment.setStatus(ShipmentStatus.BOOKED);
        sampleShipment.setUserId(2L);
        sampleShipment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateShipment() {
        CreateShipmentRequest req = new CreateShipmentRequest();
        req.setSenderName("Tech Corp");
        req.setSenderAddress("Hyderabad");
        req.setReceiverName("Retail Hub");
        req.setReceiverAddress("Vijayawada");
        req.setPackageDescription("Sensors");
        req.setPackageWeight(3.5);
        req.setCurrentLocation("Hyderabad");

        when(shipmentRepository.save(any(Shipment.class))).thenReturn(sampleShipment);

        ShipmentDto dto = shipmentService.createShipment(req, 2L);

        assertNotNull(dto);
        assertEquals("TRK-10001", dto.getTrackingNumber());
        assertEquals("Hyderabad", dto.getCurrentLocation());
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    void testGetShipmentById() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(sampleShipment));

        ShipmentDto dto = shipmentService.getShipmentById(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("TRK-10001", dto.getTrackingNumber());
    }
}
