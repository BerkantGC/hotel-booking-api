package com.hotelbooking.notification_service.consumer;

import com.hotelbooking.common_model.BookingQueueDTO;
import com.hotelbooking.notification_service.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BookingConsumer {

    @RabbitListener(queues = RabbitMQConfig.RESERVATION_QUEUE)
    public void consumeReservation(BookingQueueDTO booking) {
        System.out.println("📩 Yeni rezervasyon mesajı alındı:");
        System.out.println("➡ Hotel ID: " + booking.getHotelId());
        System.out.println("➡ Check-in: " + booking.getCheckIn());
        System.out.println("➡ Check-out: " + booking.getCheckOut());
    }

}
