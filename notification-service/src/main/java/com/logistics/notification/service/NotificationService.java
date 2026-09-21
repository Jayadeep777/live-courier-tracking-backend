package com.logistics.notification.service;

import com.logistics.notification.dto.CreateNotificationRequest;
import com.logistics.notification.dto.NotificationDto;
import com.logistics.notification.model.Notification;
import com.logistics.notification.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository repository, SimpMessagingTemplate messagingTemplate) {
        this.repository = repository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public NotificationDto createNotification(CreateNotificationRequest request) {
        Notification notification = new Notification(
                request.getUserId(),
                request.getShipmentId(),
                request.getTrackingNumber(),
                request.getMessage(),
                request.getType()
        );
        Notification saved = repository.save(notification);
        NotificationDto dto = mapToDto(saved);

        // STOMP WebSocket push to user's personal channel & global channel
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + saved.getUserId(), dto);
            messagingTemplate.convertAndSend("/topic/notifications", dto);
        } catch (Exception ignored) {}

        return dto;
    }

    public List<NotificationDto> getUserNotifications(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return repository.countByUserIdAndReadStatusFalse(userId);
    }

    @Transactional
    public NotificationDto markAsRead(Long id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        notification.setReadStatus(true);
        return mapToDto(repository.save(notification));
    }

    private NotificationDto mapToDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getUserId(),
                n.getShipmentId(),
                n.getTrackingNumber(),
                n.getMessage(),
                n.getType(),
                n.getReadStatus(),
                n.getCreatedAt()
        );
    }
}
