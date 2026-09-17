package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Integer> {
    boolean existsByTripIdAndRequesterIdAndStatus(
        Integer tripId,
        Integer requesterId,
        String status);

    List<JoinRequest> findByTripIdAndStatus(
        Integer tripId,
        String status);

    Optional<JoinRequest> findByIdAndTripId(
        Integer id,
        Integer tripId);
}
