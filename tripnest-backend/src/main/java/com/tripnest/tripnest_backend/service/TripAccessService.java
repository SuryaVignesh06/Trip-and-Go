package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.TripMemberRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripAccessService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;

    public Trip getTrip(Integer tripId) {

        return tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new RuntimeException("Trip not found"));
    }

    // Owner OR any Trip Member
    public boolean hasAccess(Integer tripId, User user) {

        Trip trip = getTrip(tripId);

        // Owner has access
        if (trip.getOwner().getId().equals(user.getId())) {
            return true;
        }

        // Member has access
        return tripMemberRepository
                .findByTripIdAndUserId(
                        tripId,
                        user.getId())
                .isPresent();
    }

    public void checkAccess(Integer tripId, User user) {

        if (!hasAccess(tripId, user)) {
            throw new RuntimeException(
                    "You are not authorized to access this trip");
        }
    }

    // Owner OR GROUP_ADMIN
    public boolean isOwnerOrGroupAdmin(
            Integer tripId,
            User user) {

        Trip trip = getTrip(tripId);

        // Owner
        if (trip.getOwner().getId().equals(user.getId())) {
            return true;
        }

        // Group admin
        return tripMemberRepository
                .findByTripIdAndUserId(
                        tripId,
                        user.getId())
                .map(member ->
                        "GROUP_ADMIN".equalsIgnoreCase(
                                member.getRole()))
                .orElse(false);
    }

    public void checkOwnerOrGroupAdmin(
            Integer tripId,
            User user) {

        if (!isOwnerOrGroupAdmin(tripId, user)) {
            throw new RuntimeException(
                    "Only the Trip Owner or Group Admin can perform this action");
        }
    }

    // Owner only
    public boolean isOwner(
            Integer tripId,
            User user) {

        Trip trip = getTrip(tripId);

        return trip.getOwner()
                .getId()
                .equals(user.getId());
    }
}