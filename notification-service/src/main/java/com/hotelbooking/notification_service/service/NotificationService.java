package com.hotelbooking.notification_service.service;

import com.hotelbooking.common_model.User;
import com.hotelbooking.notification_service.dto.CreateNotificationRequest;
import com.hotelbooking.notification_service.dto.NotificationResponse;
import com.hotelbooking.notification_service.model.Notification;
import com.hotelbooking.notification_service.repository.NotificationRepository;
import com.hotelbooking.notification_service.repository.UserRepository;
import com.hotelbooking.notification_service.util.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final NotificationMapper mapper;
    private final SimpMessagingTemplate messaging;
    private final UserRepository userRepository;

    public NotificationResponse create(CreateNotificationRequest req) {
        User user = userRepository.findById(req.userId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Notification n = Notification.builder()
                .user(user)
                .message(req.message())
                .type(req.type())
                .build();
        n = repo.save(n);

        // WebSocket publish: /topic/alerts.{userId}
        messaging.convertAndSend("/topic/alerts." + req.userId(), mapper.toDto(n));

        return mapper.toDto(n);
    }

    public Page<NotificationResponse> findByUser(Long userId, Pageable p) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId, p)
                .map(mapper::toDto);
    }

    public long unseenCount(Long userId) {
        return repo.countByUserIdAndSeenFalse(userId);
    }

    @Transactional
    public void markSeen(Long id, Long userId) {
        repo.findById(id).filter(n -> n.getUser().getId().equals(userId)).ifPresent(n -> n.setSeen(true));
    }

    /** Mark all notifications as seen for the given user. */
    @Transactional
    public void markAllSeen(Long userId) {
        repo.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .forEach(n -> n.setSeen(true));
    }
}
