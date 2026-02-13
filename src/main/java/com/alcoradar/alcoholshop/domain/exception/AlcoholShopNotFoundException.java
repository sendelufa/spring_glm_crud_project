package com.alcoradar.alcoholshop.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when an AlcoholShop entity cannot be found in the repository.
 * <p>
 * This exception is typically thrown by repository implementations or service layer
 * when a requested alcohol shop does not exist in the database. It automatically
 * generates informative error messages including the entity's identifier.
 * <p>
 * Usage examples:
 * <pre>{@code
 * throw new AlcoholShopNotFoundException("Shop not found at specified location");
 * throw new AlcoholShopNotFoundException(shopId);
 * }</pre>
 *
 * @author AlcoRadar Team
 * @since 1.0
 */
public class AlcoholShopNotFoundException extends DomainException {

    /**
     * Constructs a new exception with the specified detail message.
     * <p>
     * Use this constructor when the context of the "not found" error cannot be
     * captured by an identifier alone (e.g., searching by location or attributes).
     *
     * @param message the detail message explaining why the shop was not found
     */
    public AlcoholShopNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception for an alcohol shop with the specified UUID.
     * <p>
     * This constructor automatically generates a descriptive message including
     * the shop's identifier, making it ideal for repository lookups by ID.
     *
     * @param id the UUID of the alcohol shop that could not be found
     */
    public AlcoholShopNotFoundException(UUID id) {
        super(String.format("AlcoholShop with id %s not found", id));
    }
}
