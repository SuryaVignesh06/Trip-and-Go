package com.tripnest.tripnest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class NominatimPlaceResponse {

    @JsonProperty("place_id")
    private Long placeId;

    private String name;

    @JsonProperty("display_name")
    private String displayName;

    private String lat;

    private String lon;

    private String type;

    private Map<String, String> address;
}