package com.hotelbooking.notification_service.dto;

import com.hotelbooking.notification_service.model.NotificationType;
import java.time.Instant;

public record NotificationResponse(
        Long id,
        String message,
        NotificationType type,
        boolean seen,
        Instant createdAt
) { }

