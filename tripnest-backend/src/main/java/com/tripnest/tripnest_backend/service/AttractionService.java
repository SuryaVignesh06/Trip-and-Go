package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Attraction;
import com.tripnest.tripnest_backend.entity.Destination;
import com.tripnest.tripnest_backend.repository.AttractionRepository;
import com.tripnest.tripnest_backend.repository.DestinationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttractionService {

    private final AttractionRepository attractionRepository;
    private final DestinationRepository destinationRepository;

    public List<Attraction> getAttractionsByDestination(
            Integer destinationId) {

        if (!destinationRepository
                .existsById(destinationId)) {

            throw new RuntimeException(
                    "Destination not found");
        }

        return attractionRepository
                .findByDestinationIdOrderByNameAsc(
                        destinationId);
    }

    public Attraction createAttraction(
            Integer destinationId,
            Attraction attraction) {

        if (attraction == null) {
            throw new IllegalArgumentException(
                    "Attraction data is required");
        }

        if (attraction.getName() == null
                || attraction.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Attraction name is required");
        }

        Destination destination =
                destinationRepository
                        .findById(destinationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Destination not found"));

        attraction.setId(null);
        attraction.setDestination(destination);

        return attractionRepository.save(
                attraction);
    }
}