package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.NotificationResponse;
import com.tripnest.tripnest_backend.entity.Notification;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.NotificationRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    private User getCurrentUser() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"));
    }

    public List<NotificationResponse> getMyNotifications() {

        User user = getCurrentUser();

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> getUnreadNotifications() {

        User user = getCurrentUser();

        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long getUnreadCount() {

        User user = getCurrentUser();

        return notificationRepository
                .countByUserIdAndReadFalse(user.getId());
    }

    public NotificationResponse markAsRead(
            Integer notificationId) {

        User user = getCurrentUser();

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"));

        if (!notification.getUser()
                .getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You are not authorized to update this notification");
        }

        notification.setRead(true);

        return toResponse(
                notificationRepository.save(notification));
    }

    /*
     * Creates an in-app notification.
     */
    public Notification createNotification(
            User user,
            String title,
            String message,
            String type) {

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    /*
     * Creates a notification only if the same
     * notification does not already exist for this user.
     */
    public Notification createNotificationIfNotExists(
            User user,
            String title,
            String message,
            String type) {

        boolean exists =
                notificationRepository
                        .existsByUserIdAndTypeAndMessage(
                                user.getId(),
                                type,
                                message);

        if (exists) {
            return null;
        }

        return createNotification(
                user,
                title,
                message,
                type);
    }

    /*
     * Creates both in-app notification and email.
     *
     * Email failure does not break the main
     * business operation.
     */
    public Notification createNotificationAndEmail(
            User user,
            String title,
            String message,
            String type) {

        Notification notification =
                createNotification(
                        user,
                        title,
                        message,
                        type);

        sendEmail(
                user.getEmail(),
                title,
                message);

        return notification;
    }

    public void sendEmail(
            String email,
            String subject,
            String message) {

        try {

            SimpleMailMessage mail =
                    new SimpleMailMessage();

            mail.setTo(email);
            mail.setSubject(subject);
            mail.setText(message);

            mailSender.send(mail);

        } catch (Exception e) {

            
            System.err.println(
                    "Unable to send notification email: "
                            + e.getMessage());
        }
    }

    private NotificationResponse toResponse(
            Notification notification) {

        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}