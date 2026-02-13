package com.alcoradar.alcoholshop.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration representing the type of alcohol shop.
 * <p>
 * This enum classifies shops into different categories based on their business model
 * and product assortment. Each shop type has specific characteristics that affect
 * pricing, operating hours, and available inventory.
 * <p>
 * Shop types:
 * <ul>
 *   <li>{@link #SUPERMARKET} - Large retail chain stores with general merchandise</li>
 *   <li>{@link #SPECIALTY} - Specialized alcohol-focused stores with premium selection</li>
 *   <li>{@link #DUTY_FREE} - Tax-free shops typically located at airports/border crossings</li>
 * </ul>
 *
 * @author AlcoRadar Team
 * @since 1.0
 */
@Schema(description = "Type of alcohol shop")
public enum ShopType {

    /**
     * Large supermarket chains that sell alcohol alongside general merchandise.
     * <p>
     * Characteristics:
     * <ul>
     *   <li>Wide operating hours (often 24/7)</li>
     *   <li>Mid-range pricing with regular promotions</li>
     *   <li>Standard selection of popular brands</li>
     *   <li>Multiple locations throughout cities</li>
     * </ul>
     */
    @Schema(description = "Large retail chain with general merchandise including alcohol")
    SUPERMARKET("Supermarket", "Large retail chain with general merchandise including alcohol"),

    /**
     * Specialized alcohol stores focusing on premium and rare products.
     * <p>
     * Characteristics:
     * <ul>
     *   <li>Expert staff with product knowledge</li>
     *   <li>Extended selection of premium brands</li>
     *   <li>Higher prices for exclusive products</li>
     *   <li>Limited locations, typically in city centers</li>
     * </ul>
     */
    @Schema(description = "Specialized alcohol-focused shop with premium selection")
    SPECIALTY("Specialty Store", "Specialized alcohol-focused shop with premium selection"),

    /**
     * Duty-free shops located at international airports, borders, and special zones.
     * <p>
     * Characteristics:
     * <ul>
     *   <li>Tax-exempt pricing</li>
     *   <li>International brand selection</li>
     *   <li>Access restricted to travelers</li>
     *   <li>Located in transport hubs</li>
     * </ul>
     */
    @Schema(description = "Tax-free shop typically located at airports or border crossings")
    DUTY_FREE("Duty Free", "Tax-free shop typically located at airports or border crossings");

    private final String displayName;
    private final String description;

    /**
     * Creates a new ShopType with the specified display name and description.
     *
     * @param displayName the human-readable name of this shop type
     * @param description a detailed description of this shop type
     */
    ShopType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Returns the human-readable display name of this shop type.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the detailed description of this shop type.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}
