package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Activity;
import com.tripnest.tripnest_backend.entity.Itinerary;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.ActivityRepository;
import com.tripnest.tripnest_backend.repository.ItineraryRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ItineraryRepository itineraryRepository;
    private final TripAccessService tripAccessService;
    private final UserRepository userRepository;

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    public Activity createActivity(
            Integer itineraryId,
            Activity activity) {

        User currentUser = getCurrentUser();

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() ->
                        new RuntimeException("Itinerary not found"));

        Trip trip = itinerary.getTrip();

        tripAccessService.checkOwnerOrGroupAdmin(
                trip.getId(),
                currentUser);

        activity.setItinerary(itinerary);

        return activityRepository.save(activity);
    }

    public List<Activity> getActivitiesByItineraryId(
            Integer itineraryId) {

        User currentUser = getCurrentUser();

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() ->
                        new RuntimeException("Itinerary not found"));

        Trip trip = itinerary.getTrip();

        tripAccessService.checkAccess(
                trip.getId(),
                currentUser);

        return activityRepository.findByItineraryId(itineraryId);
    }

    public Activity updateActivity(
            Integer itineraryId,
            Integer activityId,
            Activity updatedActivity) {

        User currentUser = getCurrentUser();

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() ->
                        new RuntimeException("Itinerary not found"));

        Trip trip = itinerary.getTrip();

        tripAccessService.checkOwnerOrGroupAdmin(
                trip.getId(),
                currentUser);

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() ->
                        new RuntimeException("Activity not found"));

        if (!activity.getItinerary().getId().equals(itineraryId)) {
            throw new RuntimeException(
                    "Activity does not belong to this itinerary");
        }

        activity.setName(updatedActivity.getName());
        activity.setDescription(updatedActivity.getDescription());
        activity.setLocation(updatedActivity.getLocation());
        activity.setStartTime(updatedActivity.getStartTime());
        activity.setEndTime(updatedActivity.getEndTime());

        return activityRepository.save(activity);
    }

    public void deleteActivity(
            Integer itineraryId,
            Integer activityId) {

        User currentUser = getCurrentUser();

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() ->
                        new RuntimeException("Itinerary not found"));

        Trip trip = itinerary.getTrip();

        tripAccessService.checkOwnerOrGroupAdmin(
                trip.getId(),
                currentUser);

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() ->
                        new RuntimeException("Activity not found"));

        if (!activity.getItinerary().getId().equals(itineraryId)) {
            throw new RuntimeException(
                    "Activity does not belong to this itinerary");
        }

        activityRepository.delete(activity);
    }
}