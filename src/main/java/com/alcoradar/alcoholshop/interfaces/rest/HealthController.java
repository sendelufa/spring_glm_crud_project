package com.alcoradar.alcoholshop.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.MediaType;
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
@Tag(name = "health", description = "API для проверки работоспособности сервиса")
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
    @Operation(
            summary = "Проверить работоспособность сервиса",
            description = """
                    Возвращает текущий статус работоспособности сервиса.

                    **Использование:**
                    - Load balancers проверяют этот endpoint для маршрутизации трафика
                    - Kubernetes использует для liveness/readiness probes
                    - Мониторинг системы для оповещений

                    **Ответ:**
                    - status: "UP" - сервис работает нормально
                    - Статус: 200 OK
                    """,
            tags = {"health"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сервис работает нормально",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = HealthResponse.class)
                    )
            )
    })
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP"));
    }

    /**
     * Record representing the health check response.
     *
     * @param status the health status (e.g., "UP", "DOWN")
     */
    @Schema(description = "Ответ health check endpoint")
    public record HealthResponse(
            @Schema(description = "Статус работоспособности сервиса", example = "UP")
            String status) {
    }
}
