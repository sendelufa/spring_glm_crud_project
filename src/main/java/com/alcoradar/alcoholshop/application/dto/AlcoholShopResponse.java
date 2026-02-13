package com.alcoradar.alcoholshop.application.dto;

import com.alcoradar.alcoholshop.domain.model.ShopType;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "DTO ответа с информацией об алкомаркете")
public record AlcoholShopResponse(

        /**
         * The unique identifier of the shop.
         * <p>
         * System-generated UUID that uniquely identifies this shop entity.
         */
        @Schema(
                description = "Уникальный идентификатор алкомаркета",
                example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID id,

        /**
         * The name of the alcohol shop.
         * <p>
         * Human-readable name that should be unique across all shops.
         */
        @Schema(
                description = "Название алкомаркета",
                example = "Алкомаркет на Проспекте Мира"
        )
        String name,

        /**
         * The physical address of the shop.
         * <p>
         * Full street address for navigation and identification.
         */
        @Schema(
                description = "Физический адрес алкомаркета",
                example = "г. Москва, ул. Проспект Мира, д. 123"
        )
        String address,

        /**
         * The geographical coordinates of the shop.
         * <p>
         * Contains latitude and longitude for mapping and proximity searches.
         */
        @Schema(
                description = "Географические координаты алкомаркета"
        )
        CoordinatesDto coordinates,

        /**
         * The contact phone number for the shop.
         * <p>
         * May be null if phone contact is not available.
         */
        @Schema(
                description = "Контактный номер телефона",
                example = "+7 (495) 123-45-67"
        )
        String phoneNumber,

        /**
         * The operating hours description.
         * <p>
         * Free-text description of shop schedule.
         * May be null if working hours are not specified.
         */
        @Schema(
                description = "Время работы",
                example = "Пн-Пт: 09:00-22:00, Сб-Вс: 10:00-20:00"
        )
        String workingHours,

        /**
         * The type classification of the alcohol shop.
         * <p>
         * Categorizes the shop into SUPERMARKET, SPECIALTY, or DUTY_FREE.
         * May be null if not specified.
         */
        @Schema(
                description = "Тип алкомаркета",
                example = "SUPERMARKET"
        )
        ShopType shopType,

        /**
         * The timestamp when this shop was created.
         * <p>
         * Automatically set by the system upon creation.
         */
        @Schema(
                description = "Время создания записи в системе",
                example = "2025-02-14T10:30:00"
        )
        LocalDateTime createdAt
) {
}
