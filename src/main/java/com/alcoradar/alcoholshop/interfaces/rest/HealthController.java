package com.alcoradar.alcoholshop.interfaces.rest;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for health check endpoints.
 *
 * <p>This controller provides the health check endpoint used to verify
 * that the service is running and accessible. Following Hexagonal Architecture,
 * this class belongs to the interfaces layer (REST adapter).</p>
 *
 * <p>The endpoint returns a simple status response indicating service health.</p>
 *
 * @author AlcoRadar Team
 * @version 1.0.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HealthController {

    /**
     * Health check endpoint.
     *
     * <p>Returns the current health status of the service.
     * Used by load balancers, orchestration platforms, and monitoring systems
     * to verify service availability.</p>
     *
     * <p>This custom endpoint always returns UP status as long as the
     * application context is running, regardless of downstream dependencies.</p>
     *
     * @return health response with status "UP" if service is healthy
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP"));
    }

    /**
     * Record representing the health check response.
     *
     * @param status the health status (e.g., "UP", "DOWN")
     */
    public record HealthResponse(String status) {
    }
}
