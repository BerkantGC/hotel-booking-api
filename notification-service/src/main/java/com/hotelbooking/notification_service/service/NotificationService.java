package com.hotelbooking.notification_service.service;

import com.hotelbooking.common_model.User;
import com.hotelbooking.notification_service.dto.CreateNotificationRequest;
import com.hotelbooking.notification_service.dto.NotificationResponse;
import com.hotelbooking.notification_service.model.Notification;
import com.hotelbooking.notification_service.repository.NotificationRepository;
import com.hotelbooking.notification_service.repository.UserRepository;
import com.hotelbooking.notification_service.util.GetHeaders;
import com.hotelbooking.notification_service.util.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final RestTemplate restTemplate;
    @Value("${socket.service_url}")
    private String socketServiceUrl;
    @Value("${internal.secret.key}")
    private String inter;

    private final NotificationRepository repo;
    private final NotificationMapper mapper;
    private final UserRepository userRepository;

    public NotificationResponse create(CreateNotificationRequest req) {
        User user = userRepository.findById(req.userId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Notification n = Notification.builder()
                .user(user)
                .message(req.message())
                .type(req.type())
                .build();
        n = repo.save(n);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("notification", n);

        HttpHeaders headers = GetHeaders.getHeaders(inter);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String url = String.format("%s/api/v1/notify", socketServiceUrl);
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

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
