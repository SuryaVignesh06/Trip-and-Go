package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(
            Integer userId);

    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(
            Integer userId);

    long countByUserIdAndReadFalse(
            Integer userId);

    boolean existsByUserIdAndTypeAndMessage(
            Integer userId,
            String type,
            String message);
}