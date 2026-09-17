package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Activity;
import com.tripnest.tripnest_backend.entity.Itinerary;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.TripMember;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.ActivityRepository;
import com.tripnest.tripnest_backend.repository.ItineraryRepository;
import com.tripnest.tripnest_backend.repository.TripMemberRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final TripRepository tripRepository;
    private final ItineraryRepository itineraryRepository;
    private final ActivityRepository activityRepository;
    private final TripMemberRepository tripMemberRepository;
    private final NotificationService notificationService;

    /*
     * Runs every day at 8:00 AM.
     *
     * Checks:
     * 1. Trips starting tomorrow
     * 2. Activities happening within the next 24 hours
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReminders() {

        LocalDate today = LocalDate.now();

        sendTripReminders(today);

        sendActivityReminders(today);
    }

    /*
     * Trip reminder:
     * Notify owner and members when the trip starts tomorrow.
     */
    private void sendTripReminders(LocalDate today) {

        LocalDate tomorrow = today.plusDays(1);

        List<Trip> trips = tripRepository.findAll();

        for (Trip trip : trips) {

            if (trip.getStartDate() == null) {
                continue;
            }

            if (!trip.getStartDate().equals(tomorrow)) {
                continue;
            }

            String title = "Upcoming Trip Reminder";

            String message =
                    "Your trip \""
                            + trip.getTitle()
                            + "\" starts tomorrow ("
                            + trip.getStartDate()
                            + ").";

            notifyUser(
                    trip.getOwner(),
                    title,
                    message,
                    "TRIP_REMINDER");

            List<TripMember> members =
                    tripMemberRepository.findByTripId(
                            trip.getId());

            for (TripMember member : members) {

                notifyUser(
                        member.getUser(),
                        title,
                        message,
                        "TRIP_REMINDER");
            }
        }
    }

    /*
     * Activity reminder:
     *
     * Checks activities scheduled for today or tomorrow.
     * Duplicate notifications are prevented by
     * NotificationService.createNotificationIfNotExists().
     */
    private void sendActivityReminders(LocalDate today) {

        LocalDate tomorrow = today.plusDays(1);

        List<Trip> trips = tripRepository.findAll();

        for (Trip trip : trips) {

            if (trip.getStartDate() == null) {
                continue;
            }

            List<Itinerary> itineraries =
                    itineraryRepository.findByTripId(
                            trip.getId());

            for (Itinerary itinerary : itineraries) {

                if (itinerary.getDayNumber() == null) {
                    continue;
                }

                LocalDate activityDate =
                        trip.getStartDate()
                                .plusDays(
                                        itinerary.getDayNumber() - 1L);

                if (!activityDate.equals(today)
                        && !activityDate.equals(tomorrow)) {

                    continue;
                }

                List<Activity> activities =
                        activityRepository
                                .findByItineraryId(
                                        itinerary.getId());

                for (Activity activity : activities) {

                    if (isWithinNext24Hours(
                            activityDate,
                            activity.getStartTime(),
                            today)) {

                        sendReminderForActivity(
                                trip,
                                activity,
                                activityDate);
                    }
                }
            }
        }
    }

    private boolean isWithinNext24Hours(
            LocalDate activityDate,
            LocalTime activityStartTime,
            LocalDate today) {

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime activityDateTime =
                LocalDateTime.of(
                        activityDate,
                        activityStartTime != null
                                ? activityStartTime
                                : LocalTime.MIDNIGHT);

        return !activityDateTime.isBefore(now)
                && !activityDateTime.isAfter(
                        now.plusDays(1));
    }

    private void sendReminderForActivity(
            Trip trip,
            Activity activity,
            LocalDate activityDate) {

        String timeText =
                activity.getStartTime() != null
                        ? activity.getStartTime().toString()
                        : "scheduled time";

        String title =
                "Activity Reminder";

        String message =
                "Reminder: "
                        + activity.getName()
                        + " is scheduled on "
                        + activityDate
                        + " at "
                        + timeText
                        + " during your trip \""
                        + trip.getTitle()
                        + "\".";

        notifyUser(
                trip.getOwner(),
                title,
                message,
                "ACTIVITY_REMINDER");

        List<TripMember> members =
                tripMemberRepository.findByTripId(
                        trip.getId());

        for (TripMember member : members) {

            notifyUser(
                    member.getUser(),
                    title,
                    message,
                    "ACTIVITY_REMINDER");
        }
    }

    private void notifyUser(
            User user,
            String title,
            String message,
            String type) {

        notificationService
                .createNotificationIfNotExists(
                        user,
                        title,
                        message,
                        type);
    }
}