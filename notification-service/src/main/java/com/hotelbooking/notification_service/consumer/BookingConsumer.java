package com.hotelbooking.notification_service.consumer;

import com.hotelbooking.common_model.BookingQueueDTO;
import com.hotelbooking.notification_service.config.RabbitMQConfig;
import com.hotelbooking.notification_service.dto.CreateNotificationRequest;
import com.hotelbooking.notification_service.model.NotificationType;
import com.hotelbooking.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.RESERVATION_QUEUE)
    public void consumeReservation(BookingQueueDTO booking) {
        String msg = String.format(
                "Rezervasyonunuz (#%d) alındı. Check-in: %s, Check-out: %s",
                booking.getId(),
                booking.getCheckIn(),
                booking.getCheckOut());

        /* 2) Kalıcı kayıt + WebSocket publish */
        notificationService.create(new CreateNotificationRequest(
                booking.getUserId(),
                msg,
                NotificationType.BOOKING_CREATED
        ));

        System.out.printf("📨 Bildirim oluşturuldu (user %d)\n", booking.getUserId());
    }
}
