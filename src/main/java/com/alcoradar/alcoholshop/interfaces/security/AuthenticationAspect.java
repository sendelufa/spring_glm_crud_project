package com.alcoradar.alcoholshop.interfaces.security;

import com.alcoradar.alcoholshop.application.service.SecurityService;
import com.alcoradar.alcoholshop.domain.exception.AccessDeniedException;
import com.alcoradar.alcoholshop.domain.exception.ExpiredTokenException;
import com.alcoradar.alcoholshop.domain.exception.InvalidTokenException;
import com.alcoradar.alcoholshop.domain.model.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * AOP aspect for enforcing authentication and authorization on methods annotated with {@link RequireAuth}.
 * <p>
 * This aspect intercepts all methods annotated with {@code @RequireAuth} and performs:
 * <ul>
 *   <li>Extracts the JWT token from the {@code Authorization} header</li>
 *   <li>Validates the token using {@link SecurityService#validateAccessToken(String)}</li>
 *   <li>Checks if the user's role matches any of the required roles (if specified)</li>
 *   <li>Allows the method to proceed if all checks pass</li>
 * </ul>
 * <p>
 * The aspect requires an {@link HttpServletRequest} parameter in the intercepted method's signature
 * to extract the Authorization header. If the request is not found, an {@link IllegalStateException}
 * is thrown.
 * <p>
 * Exception handling:
 * <ul>
 *   <li>{@link InvalidTokenException} - thrown for missing, invalid, or malformed tokens</li>
 *   <li>{@link ExpiredTokenException} - thrown for expired tokens</li>
 *   <li>{@link AccessDeniedException} - thrown when user's role doesn't match required roles</li>
 *   <li>{@link IllegalStateException} - thrown when HttpServletRequest is not in method signature</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>{@code
 * @RestController
 * public class UserController {
 *
 *     @GetMapping("/admin/users")
 *     @RequireAuth(roles = {Role.ADMIN})
 *     public ResponseEntity<List<User>> getAllUsers(HttpServletRequest request) {
 *         // Only ADMIN role users can access this endpoint
 *         return ResponseEntity.ok(userService.findAll());
 *     }
 * }
 * }</pre>
 *
 * @see RequireAuth
 * @see SecurityService
 * @since 1.0
 */
@Slf4j
@Aspect
@Component
public class AuthenticationAspect {

    private final SecurityService securityService;

    public AuthenticationAspect(SecurityService securityService) {
        this.securityService = securityService;
    }

    /**
     * Around advice for methods annotated with {@link RequireAuth}.
     * <p>
     * Performs authentication and authorization checks before allowing the method to proceed.
     *
     * @param joinPoint the join point representing the method execution
     * @param requireAuth the annotation instance containing role requirements
     * @return the result of the method execution
     * @throws Throwable if authentication or authorization fails, or if the method execution throws
     */
    @Around("@annotation(com.alcoradar.alcoholshop.interfaces.security.RequireAuth)")
    public Object authenticate(ProceedingJoinPoint joinPoint, RequireAuth requireAuth) throws Throwable {
        log.debug("AuthenticationAspect: Intercepting method {}", joinPoint.getSignature().toShortString());

        // Extract Authorization header from HttpServletRequest
        HttpServletRequest request = getRequestFromJoinPoint(joinPoint);
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("AuthenticationAspect: Missing or invalid Authorization header");
            throw new InvalidTokenException("Missing or invalid Authorization header");
        }

        String token = authorizationHeader.substring(7); // Remove "Bearer " prefix

        try {
            // Validate JWT and extract claims
            Claims claims = securityService.validateAccessToken(token);

            // Extract userId and set as request attribute for controllers to use
            UUID userId = securityService.getUserIdFromToken(claims);
            request.setAttribute("userId", userId);

            // Check role requirements
            if (!hasRequiredRole(claims, requireAuth.roles())) {
                String userRoleStr = claims.get("role", String.class);
                Role userRole = Role.valueOf(userRoleStr);
                log.warn("AuthenticationAspect: Access denied for user with role {}, required roles: {}",
                        userRole, Arrays.toString(requireAuth.roles()));
                throw new AccessDeniedException(requireAuth.roles()[0], userRole);
            }

            log.debug("AuthenticationAspect: Authentication successful for user with role {}",
                    claims.get("role", String.class));

            // Proceed with authenticated request
            return joinPoint.proceed();
        } catch (ExpiredTokenException e) {
            log.warn("AuthenticationAspect: Expired token");
            throw e;
        } catch (InvalidTokenException e) {
            log.warn("AuthenticationAspect: Invalid token - {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if the user's role matches any of the required roles.
     * <p>
     * If no roles are specified (empty array), all authenticated users are allowed.
     *
     * @param claims the JWT claims containing the user's role
     * @param requiredRoles the array of roles required to access the method
     * @return {@code true} if the user has at least one of the required roles, or if no roles are required
     */
    private boolean hasRequiredRole(Claims claims, Role[] requiredRoles) {
        if (requiredRoles == null || requiredRoles.length == 0) {
            log.debug("AuthenticationAspect: No role requirement specified, allowing all authenticated users");
            return true; // No role requirement = allow all authenticated users
        }

        String userRoleStr = claims.get("role", String.class);
        Role userRole = Role.valueOf(userRoleStr);

        boolean hasRole = Arrays.stream(requiredRoles)
                .anyMatch(requiredRole -> requiredRole == userRole);

        if (hasRole) {
            log.debug("AuthenticationAspect: User role {} matches required roles", userRole);
        } else {
            log.debug("AuthenticationAspect: User role {} does not match required roles {}", userRole, Arrays.toString(requiredRoles));
        }

        return hasRole;
    }

    /**
     * Extracts the HttpServletRequest from the method's parameter list.
     * <p>
     * The intercepted method must include HttpServletRequest in its signature.
     *
     * @param joinPoint the join point containing method arguments
     * @return the HttpServletRequest from the method signature
     * @throws IllegalStateException if HttpServletRequest is not found in the method signature
     */
    private HttpServletRequest getRequestFromJoinPoint(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(arg -> arg instanceof HttpServletRequest)
                .map(arg -> (HttpServletRequest) arg)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("AuthenticationAspect: HttpServletRequest not found in method signature for {}",
                            joinPoint.getSignature().toShortString());
                    return new IllegalStateException(
                            "HttpServletRequest not found in method signature. " +
                                    "Methods annotated with @RequireAuth must include HttpServletRequest parameter.");
                });
    }
}
