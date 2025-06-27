package com.hotelbooking.notification_service.repository;

import com.hotelbooking.common_model.User;
import com.hotelbooking.notification_service.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserAndSeenFalse(User user, Pageable p);
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndSeenFalse(Long userId);
}
