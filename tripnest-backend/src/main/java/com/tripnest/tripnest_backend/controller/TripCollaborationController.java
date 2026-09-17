package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.TripMemberResponse;
import com.tripnest.tripnest_backend.dto.TripSearchResponse;
import com.tripnest.tripnest_backend.entity.JoinRequest;
import com.tripnest.tripnest_backend.service.TripCollaborationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip-collaboration")
@RequiredArgsConstructor
public class TripCollaborationController {

    private final TripCollaborationService collaborationService;

    @GetMapping("/{tripId}/members")
    public List<TripMemberResponse> getMembers(
            @PathVariable Integer tripId) {

        return collaborationService.getMembers(tripId);
    }

    @PostMapping("/{tripId}/members")
    public TripMemberResponse inviteMember(
            @PathVariable Integer tripId,
            @RequestParam String email) {

        return collaborationService.inviteMember(tripId, email);
    }

    @PutMapping("/{tripId}/members/{userId}/role")
    public TripMemberResponse updateMemberRole(
            @PathVariable Integer tripId,
            @PathVariable Integer userId,
            @RequestParam String role) {

        return collaborationService.updateMemberRole(
                tripId,
                userId,
                role);
    }

    @DeleteMapping("/{tripId}/members/{userId}")
    public void removeMember(
            @PathVariable Integer tripId,
            @PathVariable Integer userId) {

        collaborationService.removeMember(
                tripId,
                userId);
    }

    @GetMapping("/search")
    public List<TripSearchResponse> searchTrips(
            @RequestParam String name) {

        return collaborationService.searchTrips(name);
    }

    @PostMapping("/{tripId}/join-request")
    public void requestToJoin(
            @PathVariable Integer tripId) {

        collaborationService.requestToJoin(tripId);
    }

    @GetMapping("/{tripId}/join-requests")
    public List<JoinRequest> getJoinRequests(
            @PathVariable Integer tripId) {

        return collaborationService.getJoinRequests(tripId);
    }

    @PostMapping("/{tripId}/join-requests/{requestId}/approve")
    public void approveJoinRequest(
            @PathVariable Integer tripId,
            @PathVariable Integer requestId) {

        collaborationService.approveJoinRequest(
                tripId,
                requestId);
    }

    @PostMapping("/{tripId}/join-requests/{requestId}/reject")
    public void rejectJoinRequest(
            @PathVariable Integer tripId,
            @PathVariable Integer requestId) {

        collaborationService.rejectJoinRequest(
                tripId,
                requestId);
    }
}