package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, Integer> {

    List<Destination> findByIsPopularTrue();

    List<Destination> findByNameContainingIgnoreCaseOrCountryContainingIgnoreCase(
            String name,
            String country);
}