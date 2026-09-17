package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttractionRepository
        extends JpaRepository<Attraction, Integer> {

    List<Attraction>
    findByDestinationIdOrderByNameAsc(
            Integer destinationId);
}