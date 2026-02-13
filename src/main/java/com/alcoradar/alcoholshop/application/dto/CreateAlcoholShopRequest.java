package com.alcoradar.alcoholshop.application.dto;

import com.alcoradar.alcoholshop.domain.model.ShopType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new alcohol shop.
 * <p>
 * This record contains all required and optional fields needed to create a new shop entity.
 * Validation annotations ensure data integrity at the API boundary.
 * <p>
 * Validation rules:
 * <ul>
 *   <li>Name: required, non-blank</li>
 *   <li>Address: required, non-blank, 10-500 characters</li>
 *   <li>Coordinates: required, validated via {@link CoordinatesDto}</li>
 *   <li>Phone number: optional string for contact</li>
 *   <li>Working hours: optional string describing schedule</li>
 *   <li>Shop type: optional enum, defaults can be applied in service layer</li>
 * </ul>
 *
 * @param name         the unique name of the alcohol shop (required)
 * @param address      the physical address of the shop, must be at least 10 characters (required)
 * @param coordinates  the geographical coordinates of the shop location (required)
 * @param phoneNumber  the contact phone number (optional)
 * @param workingHours the operating hours description (optional)
 * @param shopType     the classification of shop type (optional)
 * @author AlcoRadar Team
 * @since 1.0
 */
public record CreateAlcoholShopRequest(

        /**
         * The name of the alcohol shop.
         * <p>
         * Must be non-blank and unique across all shops.
         */
        @NotBlank(message = "Shop name is required")
        String name,

        /**
         * The physical address of the shop.
         * <p>
         * Must be non-blank with sufficient detail (10-500 characters).
         * This helps with location identification and mapping.
         */
        @NotBlank(message = "Address is required")
        @Size(min = 10, max = 500, message = "Address must be between 10 and 500 characters")
        String address,

        /**
         * The geographical coordinates of the shop.
         * <p>
         * Nested validation is applied to ensure valid latitude/longitude values.
         */
        @Valid
        CoordinatesDto coordinates,

        /**
         * The contact phone number for the shop.
         * <p>
         * Optional field - can be null if phone contact is not available.
         * Format is not enforced to accommodate international formats.
         */
        String phoneNumber,

        /**
         * The operating hours description.
         * <p>
         * Optional field - free-text description of shop schedule.
         * Examples: "09:00-22:00", "Mon-Fri: 10:00-20:00, Sat-Sun: 11:00-18:00"
         */
        String workingHours,

        /**
         * The type classification of the alcohol shop.
         * <p>
         * Optional field - defaults may be applied by the service layer.
         * See {@link ShopType} for available categories.
         */
        ShopType shopType
) {
}
