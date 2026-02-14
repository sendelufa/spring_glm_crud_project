package com.alcoradar.alcoholshop.interfaces.security;

import com.alcoradar.alcoholshop.application.service.SecurityService;
import com.alcoradar.alcoholshop.domain.model.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT Authentication Filter that intercepts all HTTP requests to protected endpoints.
 * <p>
 * This filter checks for valid JWT access tokens in the Authorization header.
 * It runs ONCE per request (extending {@link OncePerRequestFilter}) and performs:
 * <ul>
 *   <li>Extracts JWT token from {@code Authorization: Bearer <token>} header</li>
 *   <li>Validates token using {@link SecurityService#validateAccessToken(String)}</li>
 *   <li>Extracts user ID and sets it as request attribute for controllers to use</li>
 *   <li>Allows request to proceed if token is valid</li>
 *   <li>Returns 401 Unauthorized if token is missing, invalid, or expired</li>
 * </ul>
 * <p>
 * Controllers can access the authenticated user ID via:
 * <pre>{@code
 * @GetMapping("/api/protected")
 * public ResponseEntity<?> protectedEndpoint(HttpServletRequest request) {
 *     UUID userId = (UUID) request.getAttribute("userId");
 *     // userId is guaranteed to be non-null if request reaches here
 * }
 * }</pre>
 * <p>
 * To protect endpoints, use {@link RequireAuth} annotation which checks for userId attribute:
 * <pre>{@code
 * @RequireAuth
 * @GetMapping("/api/admin/users")
 * public ResponseEntity<?> adminEndpoint(HttpServletRequest request) {
 *     // Only reaches here if valid token was provided
 * }
 * }</pre>
 *
 * @see RequireAuth
 * @see SecurityService
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityService securityService;

    /**
     * The prefix for Bearer token in Authorization header.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * The request attribute name where authenticated user ID is stored.
     */
    public static final String USER_ID_ATTRIBUTE = "userId";

    /**
     * The request attribute name where authenticated user role is stored.
     */
    public static final String USER_ROLE_ATTRIBUTE = "userRole";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                      HttpServletResponse response,
                                      FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        log.debug("JwtAuthenticationFilter: Processing request to {}", path);

        // Skip authentication for public endpoints (login, refresh, health, etc.)
        if (isPublicEndpoint(path)) {
            log.debug("JwtAuthenticationFilter: Public endpoint, skipping auth");
            filterChain.doFilter(request, response);
            return;
        }

        // Protected endpoint - requires authentication
        // Extract Authorization header
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            log.warn("JwtAuthenticationFilter: Missing or invalid Authorization header for {}", path);
            sendUnauthorizedResponse(response, "Missing or invalid Authorization header");
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        try {
            // Validate JWT and extract claims
            Claims claims = securityService.validateAccessToken(token);

            // Extract userId and userRole from JWT claims
            UUID userId = securityService.getUserIdFromToken(claims);
            Role userRole = securityService.getRoleFromToken(claims);

            // Set both as request attributes for controllers/aspects to use
            request.setAttribute(USER_ID_ATTRIBUTE, userId);
            request.setAttribute(USER_ROLE_ATTRIBUTE, userRole);

            log.debug("JwtAuthenticationFilter: Authentication successful for user {} with role {}", userId, userRole);

            // Proceed with authenticated request
            filterChain.doFilter(request, response);

        } catch (com.alcoradar.alcoholshop.domain.exception.ExpiredTokenException e) {
            log.warn("JwtAuthenticationFilter: Expired token for {}", path);
            sendUnauthorizedResponse(response, "Token has expired");
        } catch (com.alcoradar.alcoholshop.domain.exception.InvalidTokenException e) {
            log.warn("JwtAuthenticationFilter: Invalid token for {} - {}", path, e.getMessage());
            sendUnauthorizedResponse(response, "Invalid token");
        }
    }

    /**
     * Checks if the given path is a public endpoint that doesn't require authentication.
     * <p>
     * Public endpoints include:
     * <ul>
     *   <li>{@code /api/auth/login} - user login</li>
     *   <li>{@code /api/auth/refresh} - token refresh</li>
     *   <li>{@code /actuator/**} - health checks and metrics</li>
     *   <li>{@code /swagger-ui/**}, {@code /api-docs/**} - API documentation</li>
     *   <li>{@code /webjars/**} - static web resources</li>
     * </ul>
     *
     * @param path the request path
     * @return {@code true} if the path is public and should skip authentication
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/refresh") ||
                path.startsWith("/api/test/") ||  // Test data endpoints (development only)
                path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/v3/api-docs") ||  // SpringDoc OpenAPI JSON
                path.startsWith("/webjars") ||
                path.equals("/") ||
                path.startsWith("/error");
    }

    /**
     * Sends an HTTP 401 Unauthorized response.
     *
     * @param response the HTTP servlet response
     * @param message the error message to include in response body
     * @throws IOException if writing response fails
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        String jsonResponse = String.format("""
                {
                  "type": "https://api.alcoradar.com/errors/unauthorized",
                  "title": "Unauthorized",
                  "status": 401,
                  "detail": "%s",
                  "instance": "%s"
                }
                """, message, "/api/auth");
        response.getWriter().write(jsonResponse);
    }
}
