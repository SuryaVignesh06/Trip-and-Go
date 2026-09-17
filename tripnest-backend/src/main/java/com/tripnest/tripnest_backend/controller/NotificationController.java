package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.NotificationResponse;
import com.tripnest.tripnest_backend.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> getMyNotifications() {

        return notificationService
                .getMyNotifications();
    }

    @GetMapping("/unread")
    public List<NotificationResponse> getUnreadNotifications() {

        return notificationService
                .getUnreadNotifications();
    }

    @GetMapping("/unread/count")
    public long getUnreadCount() {

        return notificationService
                .getUnreadCount();
    }

    @PutMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @PathVariable Integer notificationId) {

        return notificationService
                .markAsRead(notificationId);
    }
}