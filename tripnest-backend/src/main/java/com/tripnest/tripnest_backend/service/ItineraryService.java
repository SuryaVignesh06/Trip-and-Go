package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Itinerary;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.ItineraryRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TripRepository tripRepository;
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

    public Itinerary createItinerary(
            Integer tripId,
            Itinerary itinerary) {

        User currentUser = getCurrentUser();

        Trip trip = tripAccessService.getTrip(tripId);

        tripAccessService.checkOwnerOrGroupAdmin(
                tripId,
                currentUser);

        itinerary.setTrip(trip);

        return itineraryRepository.save(itinerary);
    }

    public List<Itinerary> getItinerariesByTripId(
            Integer tripId) {

        User currentUser = getCurrentUser();

        tripAccessService.checkAccess(
                tripId,
                currentUser);

        return itineraryRepository.findByTripId(tripId);
    }
}