package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.WeatherResponse;
import com.tripnest.tripnest_backend.service.WeatherService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public WeatherResponse getWeather(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country) {

        if (latitude != null && longitude != null) {
            return weatherService.getWeather(latitude, longitude);
        }

        return weatherService.getWeather(city, country);
    }
}