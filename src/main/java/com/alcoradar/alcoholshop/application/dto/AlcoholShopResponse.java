package com.alcoradar.alcoholshop.application.dto;

import com.alcoradar.alcoholshop.domain.model.ShopType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing an alcohol shop in the API.
 * <p>
 * This record encapsulates all shop data returned to clients, including
 * the system-generated ID, all shop details, and metadata such as creation timestamp.
 * <p>
 * Used for read operations (GET endpoints) to provide a complete view of a shop
 * without exposing internal domain model details directly.
 *
 * @param id           the unique identifier of the shop
 * @param name         the name of the alcohol shop
 * @param address      the physical address of the shop
 * @param coordinates  the geographical coordinates of the shop location
 * @param phoneNumber  the contact phone number (may be null)
 * @param workingHours the operating hours description (may be null)
 * @param shopType     the classification of shop type (may be null)
 * @param createdAt    the timestamp when this shop was created in the system
 * @author AlcoRadar Team
 * @since 1.0
 */
public record AlcoholShopResponse(

        /**
         * The unique identifier of the shop.
         * <p>
         * System-generated UUID that uniquely identifies this shop entity.
         */
        UUID id,

        /**
         * The name of the alcohol shop.
         * <p>
         * Human-readable name that should be unique across all shops.
         */
        String name,

        /**
         * The physical address of the shop.
         * <p>
         * Full street address for navigation and identification.
         */
        String address,

        /**
         * The geographical coordinates of the shop.
         * <p>
         * Contains latitude and longitude for mapping and proximity searches.
         */
        CoordinatesDto coordinates,

        /**
         * The contact phone number for the shop.
         * <p>
         * May be null if phone contact is not available.
         */
        String phoneNumber,

        /**
         * The operating hours description.
         * <p>
         * Free-text description of shop schedule.
         * May be null if working hours are not specified.
         */
        String workingHours,

        /**
         * The type classification of the alcohol shop.
         * <p>
         * Categorizes the shop into SUPERMARKET, SPECIALTY, or DUTY_FREE.
         * May be null if not specified.
         */
        ShopType shopType,

        /**
         * The timestamp when this shop was created.
         * <p>
         * Automatically set by the system upon creation.
         */
        LocalDateTime createdAt
) {
}
