package com.tripnest.tripnest_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "destinations")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Destination {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String name;

	private String country;

	private String description;

	private Double latitude;

    private Double longitude;

	@Column(name = "weather_info")
	private String weatherInfo;

	@Column(name = "is_popular")
	private Boolean isPopular;
	
}
