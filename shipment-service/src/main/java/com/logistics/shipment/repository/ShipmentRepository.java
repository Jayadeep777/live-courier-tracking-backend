package com.logistics.shipment.repository;

import com.logistics.shipment.model.Shipment;
import com.logistics.shipment.model.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByUserId(Long userId);
    List<Shipment> findByStatus(ShipmentStatus status);
    List<Shipment> findByCourierId(Long courierId);
    List<Shipment> findByCourierIdOrCourierName(Long courierId, String courierName);
}
