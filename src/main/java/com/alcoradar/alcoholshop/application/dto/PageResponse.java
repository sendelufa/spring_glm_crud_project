package com.alcoradar.alcoholshop.application.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic response DTO for paginated data.
 * <p>
 * This record wraps Spring Data's {@link Page} object into a simpler, client-friendly format
 * suitable for JSON serialization in REST APIs. It provides all essential pagination metadata:
 * <ul>
 *   <li>Content: the actual data items for the current page</li>
 *   <li>Current page: zero-based page number</li>
 *   <li>Page size: number of items per page</li>
 *   <li>Total elements: total count of items across all pages</li>
 *   <li>Total pages: total number of pages available</li>
 * </ul>
 * <p>
 * Type parameter T represents the type of items in the page (typically DTOs).
 *
 * @param <T> the type of elements in this page
 * @author AlcoRadar Team
 * @since 1.0
 */
public record PageResponse<T>(

        /**
         * The list of items for the current page.
         * <p>
         * May be empty if the current page has no data or if no results match the query.
         */
        List<T> content,

        /**
         * The current page number.
         * <p>
         * Zero-based index, meaning the first page is 0.
         */
        int currentPage,

        /**
         * The requested page size.
         * <p>
         * Number of items per page. The last page may contain fewer items.
         */
        int pageSize,

        /**
         * The total number of elements across all pages.
         * <p>
         * This is the count of all items matching the query, not just on this page.
         */
        long totalElements,

        /**
         * The total number of pages available.
         * <p>
         * Calculated as: ceiling(totalElements / pageSize)
         */
        int totalPages
) {

    /**
     * Factory method that converts a Spring Data {@link Page} to a {@link PageResponse}.
     * <p>
     * This method extracts all relevant information from the Page object and creates
     * a new PageResponse DTO suitable for API responses.
     * <p>
     * Example usage:
     * <pre>{@code
     * Page<AlcoholShop> shopsPage = repository.findAll(PageRequest.of(0, 10));
     * PageResponse<AlcoholShopResponse> response = PageResponse.of(
     *     shopsPage.map(mapper::toResponse)
     * );
     * }</pre>
     *
     * @param <T>  the type of elements in the page
     * @param page the Spring Data Page to convert
     * @return a new PageResponse containing the page data and metadata
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
