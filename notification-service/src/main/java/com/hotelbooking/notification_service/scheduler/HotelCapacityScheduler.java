package com.hotelbooking.notification_service.scheduler;

import com.hotelbooking.common_model.HotelCapacityInfo;
import com.hotelbooking.notification_service.dto.CreateNotificationRequest;
import com.hotelbooking.notification_service.model.NotificationType;
import com.hotelbooking.notification_service.service.HotelAdminQueryService;
import com.hotelbooking.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotelCapacityScheduler {

    private final NotificationService notificationService;
    private final HotelAdminQueryService hotelAdminQueryService; // You will create this to fetch hotel/admin/capacity info

    /**
     * This task runs every day at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkHotelCapacities() {
        log.info("🔍 Starting hotel capacity check task");

        LocalDate oneMonthLater = LocalDate.now().plusMonths(1);

        List<HotelCapacityInfo> lowCapacityHotels = hotelAdminQueryService.getHotelsWithCapacityBelowThreshold(oneMonthLater, 20);

        for (HotelCapacityInfo info : lowCapacityHotels) {
            String msg = String.format(
                    "⚠️ Otel kapasitesi %s için %s tarihine kadar %%20'nin altına düştü.",
                    info.getHotelName(), oneMonthLater.toString()
            );

            notificationService.create(new CreateNotificationRequest(
                    info.getAdminUserId(),
                    msg,
                    NotificationType.LOW_CAPACITY_WARNING
            ));
            log.info("📨 Bildirim gönderildi: {}", msg);
        }
    }
}
