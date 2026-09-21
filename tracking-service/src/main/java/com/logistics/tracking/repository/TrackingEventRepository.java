package com.logistics.tracking.repository;

import com.logistics.tracking.model.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    List<TrackingEvent> findByTrackingNumberOrderByTimestampAsc(String trackingNumber);
    List<TrackingEvent> findByShipmentIdOrderByTimestampAsc(Long shipmentId);

    @Transactional
    @Modifying
    void deleteByTrackingNumber(String trackingNumber);
}
