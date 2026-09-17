package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.TripAnalyticsResponse;
import com.tripnest.tripnest_backend.service.AnalyticsService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final AnalyticsService analyticsService;

    @GetMapping("/trip/{tripId}")
    public TripAnalyticsResponse getTripReport(
            @PathVariable Integer tripId) {

        return analyticsService
                .getTripAnalytics(tripId);
    }
}