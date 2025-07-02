package com.hotelbooking.notification_service.controller;

import com.hotelbooking.notification_service.dto.CreateNotificationRequest;
import com.hotelbooking.notification_service.dto.NotificationResponse;
import com.hotelbooking.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public Page<NotificationResponse> list(Pageable pageable, Principal principal) {
        Long uid = Long.valueOf(principal.getName());
        log.info("User {} requested notifications", uid);
        return service.findByUser(uid, pageable);
    }

    @GetMapping("/unseen-count")
    public long unseen(Principal principal) {
        return service.unseenCount(Long.valueOf(principal.getName()));
    }

    @PutMapping("/{id}/seen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markSeen(@PathVariable Long id, Principal principal) {
        service.markSeen(id, Long.valueOf(principal.getName()));
    }

    @PutMapping("/seen-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllSeen(Principal principal) {
        service.markAllSeen(Long.valueOf(principal.getName()));
    }
}
