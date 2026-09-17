package com.tripnest.tripnest_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tripnest.tripnest_backend.dto.WeatherResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherService {

    @Value("${openweather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherResponse getWeather(double latitude, double longitude) {
        return requestWeather(
                UriComponentsBuilder
                        .fromUriString("https://api.openweathermap.org/data/2.5/weather")
                        .queryParam("lat", latitude)
                        .queryParam("lon", longitude)
        );
    }

    public WeatherResponse getWeather(String city, String country) {

        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException(
                    "A city is required to retrieve weather"
            );
        }

        String location = country == null || country.isBlank()
                ? city
                : city + "," + country;

        return requestWeather(
                UriComponentsBuilder
                        .fromUriString("https://api.openweathermap.org/data/2.5/weather")
                        .queryParam("q", location)
        );
    }

    private WeatherResponse requestWeather(
            UriComponentsBuilder urlBuilder) {

        if (apiKey == null
                || apiKey.isBlank()
                || "demo-key".equals(apiKey)) {

            throw new IllegalStateException(
                    "Weather is not configured. Set OPENWEATHER_API_KEY and restart the backend."
            );
        }

        JsonNode response = restTemplate
                .getForObject(
                        urlBuilder
                                .queryParam("appid", apiKey)
                                .queryParam("units", "metric")
                                .build(true)
                                .toUri(),
                        JsonNode.class
                );

        if (response == null
                || response.path("main")
                           .path("temp")
                           .isMissingNode()) {

            throw new IllegalStateException(
                    "Weather service returned an unexpected response"
            );
        }

        return new WeatherResponse(
                response.path("main").path("temp").asDouble(),
                response.path("weather")
                        .path(0)
                        .path("description")
                        .asText("Current conditions unavailable"),
                "OpenWeather"
        );
    }
}