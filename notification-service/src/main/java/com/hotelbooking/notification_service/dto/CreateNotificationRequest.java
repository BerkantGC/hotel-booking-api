package com.hotelbooking.notification_service.dto;

import com.hotelbooking.notification_service.model.NotificationType;

public record CreateNotificationRequest(
        Long userId,
        String message,
        NotificationType type
) { }
