package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Destination;
import com.tripnest.tripnest_backend.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DestinationService {

    private final DestinationRepository destinationRepository;

    public List<Destination> getAllDestinations(String query) {

        if (query == null || query.isBlank()) {
            return destinationRepository.findAll();
        }

        String searchTerm = query.trim();

        return destinationRepository
                .findByNameContainingIgnoreCaseOrCountryContainingIgnoreCase(
                        searchTerm,
                        searchTerm);
    }

    public Destination getDestinationById(Integer id) {
        return destinationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Destination not found"));
    }

    public List<Destination> getPopularDestinations() {
        return destinationRepository.findByIsPopularTrue();
    }
}