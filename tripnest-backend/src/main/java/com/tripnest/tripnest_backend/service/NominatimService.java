package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.NominatimPlaceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Service
public class NominatimService {

    private final RestClient restClient;

    public NominatimService(
            @Value("${nominatim.base-url}") String baseUrl,
            @Value("${nominatim.user-agent}") String userAgent) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    public List<NominatimPlaceResponse> searchPlaces(String query) {

        NominatimPlaceResponse[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("format", "jsonv2")
                        .queryParam("limit", 10)
                        .queryParam("addressdetails", 1)
                        .build())
                .retrieve()
                .body(NominatimPlaceResponse[].class);

        return response == null
                ? List.of()
                : Arrays.asList(response);
    }
}