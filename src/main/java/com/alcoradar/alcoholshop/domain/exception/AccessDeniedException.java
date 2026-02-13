package com.alcoradar.alcoholshop.domain.exception;

import com.alcoradar.alcoholshop.domain.model.Role;

/**
 * Exception thrown when a user attempts to access a resource without sufficient role privileges.
 * <p>
 * This exception captures both the required role for the operation and the user's actual role,
 * enabling detailed error messages and audit logging for authorization failures.
 * <p>
 * This exception is part of the RBAC (Role-Based Access Control) implementation and should be
 * thrown when authorization checks fail due to insufficient role permissions.
 *
 * @author AlcoRadar Team
 * @since 1.0
 */
public class AccessDeniedException extends AuthenticationException {
    private final Role requiredRole;
    private final Role userRole;

    /**
     * Constructs a new access denied exception with the required and user roles.
     *
     * @param requiredRole the role required to access the resource
     * @param userRole     the role of the user attempting access
     */
    public AccessDeniedException(Role requiredRole, Role userRole) {
        super(String.format("Access denied: Required role: %s, User role: %s", requiredRole, userRole));
        this.requiredRole = requiredRole;
        this.userRole = userRole;
    }

    /**
     * Returns the role required to access the resource.
     *
     * @return the required role
     */
    public Role getRequiredRole() {
        return requiredRole;
    }

    /**
     * Returns the role of the user who attempted to access the resource.
     *
     * @return the user's role
     */
    public Role getUserRole() {
        return userRole;
    }
}
