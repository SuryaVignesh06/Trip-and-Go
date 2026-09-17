package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.NominatimPlaceResponse;
import com.tripnest.tripnest_backend.entity.Destination;
import com.tripnest.tripnest_backend.service.DestinationService;
import com.tripnest.tripnest_backend.service.NominatimService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;
    private final NominatimService nominatimService;

    @GetMapping
    public List<Destination> getAllDestinations(
            @RequestParam(required = false) String query) {
        return destinationService.getAllDestinations(query);
    }

    @GetMapping("/popular")
    public List<Destination> getPopularDestinations() {
        return destinationService.getPopularDestinations();
    }

    @GetMapping("/search")
    public List<NominatimPlaceResponse> searchPlaces(
            @RequestParam String query) {

        return nominatimService.searchPlaces(query);
    }

    @GetMapping("/{id}")
    public Destination getDestinationById(@PathVariable Integer id) {
        return destinationService.getDestinationById(id);
    }
}