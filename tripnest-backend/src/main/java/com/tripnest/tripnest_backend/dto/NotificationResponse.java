package com.tripnest.tripnest_backend.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Integer id,
        String title,
        String message,
        String type,
        boolean read,
        LocalDateTime createdAt) {
}