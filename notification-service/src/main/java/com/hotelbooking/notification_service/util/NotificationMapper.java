package com.hotelbooking.notification_service.util;
import com.hotelbooking.notification_service.dto.NotificationResponse;
import com.hotelbooking.notification_service.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toDto(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getMessage(),
                n.getType(),
                n.isSeen(),
                n.getCreatedAt()
        );
    }
}
