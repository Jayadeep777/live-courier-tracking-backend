package com.logistics.shipment.client;

import com.logistics.shipment.dto.CreateNotificationRequest;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class NotificationClientFallbackFactory implements FallbackFactory<NotificationClient> {
    @Override
    public NotificationClient create(Throwable cause) {
        return new NotificationClient() {
            @Override
            public Map<String, Object> sendNotification(CreateNotificationRequest request) {
                return Collections.emptyMap(); // Graceful fallback
            }
        };
    }
}
