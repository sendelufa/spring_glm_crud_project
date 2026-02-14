package com.alcoradar.alcoholshop.interfaces.security;

import com.alcoradar.alcoholshop.domain.model.Role;

import java.lang.annotation.*;

/**
 * Method-level annotation for enforcing authentication and role-based authorization.
 * <p>
 * When applied to a method, this annotation triggers the {@link AuthenticationAspect}
 * to intercept the method invocation and perform:
 * <ul>
 *   <li>JWT token validation from the Authorization header</li>
 *   <li>Role-based authorization check (if roles are specified)</li>
 *   <li>Exception throwing for authentication/authorization failures</li>
 * </ul>
 * <p>
 * Usage examples:
 * <pre>{@code
 * // Require authentication only (any authenticated user)
 * @RequireAuth
 * public void someMethod() { ... }
 *
 * // Require authentication with specific role
 * @RequireAuth(roles = {Role.ADMIN})
 * public void adminMethod() { ... }
 *
 * // Require authentication with multiple allowed roles
 * @RequireAuth(roles = {Role.USER, Role.ADMIN})
 * public void multiRoleMethod() { ... }
 * }</pre>
 * <p>
 * The aspect expects an {@link jakarta.servlet.http.HttpServletRequest} parameter
 * in the method signature to extract the Authorization header.
 *
 * @see AuthenticationAspect
 * @see com.alcoradar.alcoholshop.application.service.SecurityService
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAuth {

    /**
     * Array of roles authorized to access the annotated method.
     * <p>
     * If empty (default), any authenticated user can access the method.
     * If specified, the user must have at least one of the listed roles.
     *
     * @return array of allowed roles, or empty if no role requirement
     */
    Role[] roles() default {};
}
