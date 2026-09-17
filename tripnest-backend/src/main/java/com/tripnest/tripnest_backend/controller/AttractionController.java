package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Attraction;
import com.tripnest.tripnest_backend.service.AttractionService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/destinations")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    @GetMapping("/{destinationId}/attractions")
    public List<Attraction> getAttractions(
            @PathVariable Integer destinationId) {

        return attractionService
                .getAttractionsByDestination(
                        destinationId);
    }

    @PostMapping("/{destinationId}/attractions")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public Attraction createAttraction(
            @PathVariable Integer destinationId,
            @RequestBody Attraction attraction) {

        return attractionService
                .createAttraction(
                        destinationId,
                        attraction);
    }
}