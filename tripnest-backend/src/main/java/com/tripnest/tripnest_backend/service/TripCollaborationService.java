package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.TripMemberResponse;
import com.tripnest.tripnest_backend.dto.TripSearchResponse;
import com.tripnest.tripnest_backend.entity.JoinRequest;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.TripMember;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.JoinRequestRepository;
import com.tripnest.tripnest_backend.repository.TripMemberRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripCollaborationService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripMemberRepository tripMemberRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final TripAccessService tripAccessService;
    private final NotificationService notificationService;

    // GET MEMBERS
    public List<TripMemberResponse> getMembers(Integer tripId) {

        User currentUser = currentUser();

        Trip trip = tripAccessService.getTrip(tripId);

        tripAccessService.checkAccess(tripId, currentUser);

        List<TripMemberResponse> members = new ArrayList<>();

        members.add(
                toResponse(
                        trip.getOwner(),
                        "OWNER"));

        tripMemberRepository.findByTripId(tripId)
                .forEach(member ->
                        members.add(
                                toResponse(
                                        member.getUser(),
                                        member.getRole())));

        return members;
    }

    // INVITE MEMBER
    public TripMemberResponse inviteMember(
            Integer tripId,
            String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required");
        }

        User currentUser = currentUser();

        Trip trip = tripAccessService.getTrip(tripId);

        tripAccessService.checkOwnerOrGroupAdmin(
                tripId,
                currentUser);

        User user = userRepository
                .findByEmail(email.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No user exists with that email"));

        if (trip.getOwner()
                .getId()
                .equals(user.getId())) {

            throw new IllegalArgumentException(
                    "The trip owner is already a member");
        }

        if (tripMemberRepository
                .findByTripIdAndUserId(
                        tripId,
                        user.getId())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "User is already a trip member");
        }

        TripMember member = new TripMember();

        member.setTrip(trip);
        member.setUser(user);
        member.setRole("MEMBER");

        tripMemberRepository.save(member);

        notificationService.createNotificationAndEmail(
        user,
        "Added to Trip",
        "You have been added to the trip: "
                + trip.getTitle(),
        "MEMBER_ADDED");

        return toResponse(
                user,
                member.getRole());
    }

    // CHANGE MEMBER ROLE
    public TripMemberResponse updateMemberRole(
            Integer tripId,
            Integer userId,
            String role) {

        User currentUser = currentUser();

        tripAccessService.checkOwnerOrGroupAdmin(
                tripId,
                currentUser);

        if (!"MEMBER".equals(role)
                && !"GROUP_ADMIN".equals(role)) {

            throw new IllegalArgumentException(
                    "Role must be MEMBER or GROUP_ADMIN");
        }

        TripMember member =
                tripMemberRepository
                        .findByTripIdAndUserId(
                                tripId,
                                userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Trip member not found"));

        member.setRole(role);

        tripMemberRepository.save(member);

        return toResponse(
                member.getUser(),
                role);
    }

    // REMOVE MEMBER
    public void removeMember(
            Integer tripId,
            Integer userId) {

        User currentUser = currentUser();

        tripAccessService.checkOwnerOrGroupAdmin(
                tripId,
                currentUser);

        TripMember member =
                tripMemberRepository
                        .findByTripIdAndUserId(
                                tripId,
                                userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Trip member not found"));

        tripMemberRepository.delete(member);
    }

    // SEARCH TRIPS
    public List<TripSearchResponse> searchTrips(
            String name) {

        if (name == null || name.isBlank()) {
            return List.of();
        }

        return tripRepository
                .findTop20ByTitleContainingIgnoreCaseOrderByTitleAsc(
                        name.trim())
                .stream()
                .map(trip ->
                        new TripSearchResponse(
                                trip.getId(),
                                trip.getTitle(),
                                trip.getDestination().getName(),
                                trip.getOwner().getName(),
                                tripMemberRepository
                                        .findByTripId(trip.getId())
                                        .size() + 1L))
                .toList();
    }

    // REQUEST TO JOIN
    public void requestToJoin(Integer tripId) {

        User user = currentUser();

        Trip trip = tripAccessService.getTrip(tripId);

        if (trip.getOwner()
                .getId()
                .equals(user.getId())
                || tripMemberRepository
                        .findByTripIdAndUserId(
                                tripId,
                                user.getId())
                        .isPresent()) {

            throw new IllegalArgumentException(
                    "You are already a trip member");
        }

        boolean pendingRequest =
                joinRequestRepository
                        .existsByTripIdAndRequesterIdAndStatus(
                                tripId,
                                user.getId(),
                                "PENDING");

        if (pendingRequest) {
            throw new IllegalArgumentException(
                    "A join request is already pending");
        }

        JoinRequest request = new JoinRequest();

        request.setTrip(trip);
        request.setRequester(user);
        request.setStatus("PENDING");

        joinRequestRepository.save(request);

        User tripOwner = trip.getOwner();

        notificationService.createNotificationAndEmail(
        tripOwner,
        "New Join Request",
        user.getName()
                + " has requested to join your trip: "
                + trip.getTitle(),
        "JOIN_REQUEST");
    }

    // GET JOIN REQUESTS
    public List<JoinRequest> getJoinRequests(
            Integer tripId) {

        User currentUser = currentUser();

        tripAccessService.checkOwnerOrGroupAdmin(
                tripId,
                currentUser);

        return joinRequestRepository
                .findByTripIdAndStatus(
                        tripId,
                        "PENDING");
    }

    // APPROVE JOIN REQUEST
    public void approveJoinRequest(
            Integer tripId,
            Integer requestId) {

        User currentUser = currentUser();

        tripAccessService.checkOwnerOrGroupAdmin(
                tripId,
                currentUser);

        JoinRequest request =
                joinRequestRepository
                        .findByIdAndTripId(
                                requestId,
                                tripId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Join request not found"));

        if (!"PENDING".equalsIgnoreCase(
                request.getStatus())) {

            throw new IllegalArgumentException(
                    "Join request has already been processed");
        }

        User requester = request.getRequester();

        if (tripMemberRepository
                .findByTripIdAndUserId(
                        tripId,
                        requester.getId())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "User is already a trip member");
        }

        TripMember member = new TripMember();

        member.setTrip(request.getTrip());
        member.setUser(requester);
        member.setRole("MEMBER");

        tripMemberRepository.save(member);

        request.setStatus("APPROVED");

        joinRequestRepository.save(request);

        notificationService.createNotificationAndEmail(
        requester,
        "Join Request Approved",
        "Your request to join the trip '"
                + request.getTrip().getTitle()
                + "' has been approved.",
        "JOIN_REQUEST_APPROVED");
    }

    // REJECT JOIN REQUEST
    public void rejectJoinRequest(
            Integer tripId,
            Integer requestId) {

        User currentUser = currentUser();

        tripAccessService.checkOwnerOrGroupAdmin(
                tripId,
                currentUser);

        JoinRequest request =
                joinRequestRepository
                        .findByIdAndTripId(
                                requestId,
                                tripId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Join request not found"));

        if (!"PENDING".equalsIgnoreCase(
                request.getStatus())) {

            throw new IllegalArgumentException(
                    "Join request has already been processed");
        }

        request.setStatus("REJECTED");

        joinRequestRepository.save(request);

        notificationService.createNotificationAndEmail(
        request.getRequester(),
        "Join Request Rejected",
        "Your request to join the trip '"
                + request.getTrip().getTitle()
                + "' has been rejected.",
        "JOIN_REQUEST_REJECTED");
    }

    // CURRENT USER
    private User currentUser() {

        return userRepository
                .findByEmail(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user no longer exists"));
    }

    // RESPONSE
    private TripMemberResponse toResponse(
            User user,
            String role) {

        return new TripMemberResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                role);
    }
}