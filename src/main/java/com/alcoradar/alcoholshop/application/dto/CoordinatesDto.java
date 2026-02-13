package com.alcoradar.alcoholshop.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Data Transfer Object for geographical coordinates.
 * <p>
 * This record encapsulates latitude and longitude values with built-in validation
 * to ensure coordinates fall within valid ranges:
 * <ul>
 *   <li>Latitude: -90 to +90 degrees</li>
 *   <li>Longitude: -180 to +180 degrees</li>
 * </ul>
 * <p>
 * Used throughout the application for representing shop locations and user positions.
 *
 * @param latitude  the latitude coordinate in degrees, must be between -90 and 90
 * @param longitude the longitude coordinate in degrees, must be between -180 and 180
 * @author AlcoRadar Team
 * @since 1.0
 */
public record CoordinatesDto(

        /**
         * The latitude coordinate in degrees.
         * <p>
         * Must be between -90 (South Pole) and +90 (North Pole).
         * Positive values represent northern hemisphere, negative values southern.
         */
        @Min(value = -90, message = "Latitude must be at least -90 degrees")
        @Max(value = 90, message = "Latitude must be at most 90 degrees")
        Double latitude,

        /**
         * The longitude coordinate in degrees.
         * <p>
         * Must be between -180 and +180.
         * Positive values represent eastern hemisphere, negative values western.
         */
        @Min(value = -180, message = "Longitude must be at least -180 degrees")
        @Max(value = 180, message = "Longitude must be at most 180 degrees")
        Double longitude
) {
}
