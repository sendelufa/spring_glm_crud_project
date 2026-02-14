package com.alcoradar.alcoholshop.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for AlcoholShop API documentation.
 *
 * <p>This configuration class sets up interactive API documentation using SpringDoc OpenAPI.
 * It provides Swagger UI for exploring and testing REST endpoints.</p>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Swagger UI available at /swagger-ui.html</li>
 *   <li>OpenAPI JSON spec at /api-docs/openapi.json</li>
 *   <li>API versioning support</li>
 *   <li>Contact and license information</li>
 *   <li>Common error response schemas</li>
 * </ul>
 *
 * @author AlcoRadar Team
 * @version 1.0.0
 * @since 2025
 */
@Configuration
public class SwaggerConfig {

    private static final String API_TITLE = "AlcoholShop API";
    private static final String API_DESCRIPTION = """
            REST API сервис для управления алкомаркетами.

            **Возможности:**
            - Создание алкомаркетов
            - Получение информации по ID
            - Список с пагинацией и сортировкой

            **Версионирование API:**
            - Текущая версия: v1
            - Базовый путь: /api

            **Аутентификация:**
            - JWT Bearer токен требуется для защищенных эндпоинтов
            - Получите токен через POST /api/auth/login
            - Нажмите кнопку "Authorize" вверху и введите: Bearer <ваш_токен>
            """;

    private static final String API_VERSION = "1.0.0";

    /**
     * Configures the main OpenAPI documentation.
     *
     * <p>Sets up API metadata including title, description, version,
     * contact information, license details, and JWT security scheme.</p>
     *
     * @return configured OpenAPI specification
     */
    @Bean
    public OpenAPI alcoholShopOpenAPI() {
        final String SECURITY_SCHEME_NAME = "bearer-jwt";

        return new OpenAPI()
                .info(new Info()
                        .title(API_TITLE)
                        .description(API_DESCRIPTION)
                        .version(API_VERSION)
                        .contact(new Contact()
                                .name("AlcoRadar Team")
                                .email("support@alcoradar.ru")
                                .url("https://alcoradar.ru"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        // JWT Security Scheme
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT токен авторизации. Получите токен через POST /api/auth/login и введите его здесь (без префикса 'Bearer ')."))
                        // Common error responses
                        .addSchemas("BadRequestError", createBadRequestSchema())
                        .addSchemas("NotFoundError", createNotFoundSchema())
                        .addSchemas("ValidationError", createValidationSchema())
                        // Common API responses
                        .addResponses("BadRequest", createBadRequestResponse())
                        .addResponses("NotFound", createNotFoundResponse())
                        .addResponses("ValidationError", createValidationResponse()))
                // Apply security globally (can be overridden per endpoint)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * Creates a grouped OpenAPI for shops endpoints.
     *
     * <p>This allows Swagger UI to organize endpoints by logical groups.</p>
     *
     * @return GroupedOpenApi for shops controller
     */
    @Bean
    public GroupedOpenApi shopsApi() {
        return GroupedOpenApi.builder()
                .group("shops")
                .pathsToMatch("/api/shops/**")
                .build();
    }

    /**
     * Creates a grouped OpenAPI for authentication endpoints.
     *
     * @return GroupedOpenApi for auth controller
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("authentication")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    /**
     * Creates a grouped OpenAPI for health endpoints.
     *
     * @return GroupedOpenApi for health controller
     */
    @Bean
    public GroupedOpenApi healthApi() {
        return GroupedOpenApi.builder()
                .group("health")
                .pathsToMatch("/api/v1/health")
                .build();
    }

    /**
     * Creates schema for bad request error response.
     */
    private Schema<?> createBadRequestSchema() {
        return new Schema<>()
                .type("object")
                .addProperty("status", new Schema<>().type("integer").example(400))
                .addProperty("message", new Schema<>().type("string").example("Bad Request"))
                .addProperty("timestamp", new Schema<>().type("string").format("date-time"))
                .addProperty("path", new Schema<>().type("string").example("/api/shops"));
    }

    /**
     * Creates schema for not found error response.
     */
    private Schema<?> createNotFoundSchema() {
        return new Schema<>()
                .type("object")
                .addProperty("status", new Schema<>().type("integer").example(404))
                .addProperty("message", new Schema<>().type("string").example("Resource not found"))
                .addProperty("timestamp", new Schema<>().type("string").format("date-time"))
                .addProperty("path", new Schema<>().type("string").example("/api/shops/123e4567-e89b-12d3-a456-426614174000"));
    }

    /**
     * Creates schema for validation error response.
     */
    private Schema<?> createValidationSchema() {
        return new Schema<>()
                .type("object")
                .addProperty("status", new Schema<>().type("integer").example(400))
                .addProperty("message", new Schema<>().type("string").example("Validation failed"))
                .addProperty("errors", new Schema<>()
                        .type("array")
                        .items(new Schema<>()
                                .type("string")
                                .example("Name must not be blank")))
                .addProperty("timestamp", new Schema<>().type("string").format("date-time"))
                .addProperty("path", new Schema<>().type("string").example("/api/shops"));
    }

    /**
     * Creates API response for bad request (400).
     */
    private ApiResponse createBadRequestResponse() {
        return new ApiResponse()
                .description("Bad Request - Invalid input data")
                .content(new Content()
                        .addMediaType("application/json",
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/BadRequestError"))));
    }

    /**
     * Creates API response for not found (404).
     */
    private ApiResponse createNotFoundResponse() {
        return new ApiResponse()
                .description("Not Found - Resource does not exist")
                .content(new Content()
                        .addMediaType("application/json",
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/NotFoundError"))));
    }

    /**
     * Creates API response for validation errors (400).
     */
    private ApiResponse createValidationResponse() {
        return new ApiResponse()
                .description("Validation Error - Request validation failed")
                .content(new Content()
                        .addMediaType("application/json",
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ValidationError"))));
    }
}
