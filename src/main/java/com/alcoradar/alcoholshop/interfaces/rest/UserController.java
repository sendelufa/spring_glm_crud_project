package com.alcoradar.alcoholshop.interfaces.rest;

import com.alcoradar.alcoholshop.application.dto.CreateUserRequest;
import com.alcoradar.alcoholshop.application.dto.UserResponse;
import com.alcoradar.alcoholshop.application.usecase.AuthenticationUseCase;
import com.alcoradar.alcoholshop.domain.exception.UserNotFoundException;
import com.alcoradar.alcoholshop.domain.model.Role;
import com.alcoradar.alcoholshop.domain.repository.UserRepository;
import com.alcoradar.alcoholshop.interfaces.security.RequireAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for user management operations.
 *
 * <p>This controller provides user management endpoints including user creation
 * and user lookup. Follows Clean Architecture principles as part of the
 * interfaces layer (REST adapter).</p>
 *
 * <p><b>Endpoints:</b></p>
 * <ul>
 *   <li>POST /api/users - create a new user account (admin only)</li>
 *   <li>GET /api/users/{id} - get user by ID (admin only)</li>
 * </ul>
 *
 * <p><b>Security:</b></p>
 * <ul>
 *   <li>All endpoints require ADMIN role</li>
 *   <li>JWT access token must be present in Authorization header</li>
 *   <li>Token is validated by {@link com.alcoradar.alcoholshop.interfaces.security.AuthenticationAspect}</li>
 * </ul>
 *
 * <p><b>Architecture:</b></p>
 * <pre>
 * REST Controller (interfaces/rest) ← this class
 *    ↓ delegates to
 * Use Case (application/usecase)
 *    ↓ uses
 * Repository Port (domain/repository)
 *    ↓ implemented by
 * Repository Adapter (infrastructure/persistence)
 * </pre>
 *
 * @author AlcoRadar Team
 * @version 1.0.0
 * @since 2025
 * @see AuthenticationUseCase
 * @see CreateUserRequest
 * @see UserResponse
 * @see RequireAuth
 */
@Tag(name = "users", description = "User management endpoints (admin only)")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationUseCase authenticationUseCase;
    private final UserRepository userRepository;

    /**
     * Creates a new user account.
     *
     * <p>Accepts {@link CreateUserRequest} with user data, validates it, hashes the password,
     * and creates a new user through {@link AuthenticationUseCase}.</p>
     *
     * <p><b>IMPORTANT:</b> This endpoint requires ADMIN role. Only authenticated administrators
     * can create new users.</p>
     *
     * <p>Password requirements:</p>
     * <ul>
     *   <li>Minimum 8 characters</li>
     *   <li>Must contain uppercase letter</li>
     *   <li>Must contain lowercase letter</li>
     *   <li>Must contain digit</li>
     *   <li>Must contain special character (@#$%^&+=!)</li>
     * </ul>
     *
     * <p>Upon successful creation, returns HTTP 200 OK with the created user information.
     * Username already exists results in HTTP 409 Conflict.</p>
     *
     * @param request DTO with username, password, and role
     * @return DTO with created user information
     */
    @Operation(
            summary = "Create user",
            description = """
                    Register a new user account. Requires ADMIN role.

                    **Requires:**
                    - Valid JWT access token with ADMIN role

                    **Request Body:**
                    - username: user login name (3-50 characters)
                    - password: user password (min 8 chars, must contain uppercase, lowercase, digit, special char)
                    - role: user role (USER or ADMIN)

                    **Response:**
                    - id: generated user UUID
                    - username: user login name
                    - role: assigned role
                    - createdAt: account creation timestamp

                    **Error Responses:**
                    - 403 Forbidden: authenticated user lacks ADMIN role
                    - 401 Unauthorized: not authenticated
                    - 409 Conflict: username already exists
                    - 400 Bad Request: validation errors
                    """,
            tags = {"users"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - requires ADMIN role",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ForbiddenError"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/UnauthorizedError"))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username already exists",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ConflictError"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationError"))
            )
    })
    @PostMapping
    @RequireAuth(roles = {Role.ADMIN})
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = authenticationUseCase.createUser(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets user information by ID.
     *
     * <p><b>IMPORTANT:</b> This endpoint requires ADMIN role. Only authenticated administrators
     * can lookup user information by ID.</p>
     *
     * <p>Retrieves the user from the repository and returns their information.
     * If the user doesn't exist, returns HTTP 404 Not Found.</p>
     *
     * @param id the user UUID
     * @param request HTTP servlet request (used for authentication by aspect)
     * @return DTO with user information
     */
    @Operation(
            summary = "Get user by ID",
            description = """
                    Get user information by ID. Requires ADMIN role.

                    **Requires:**
                    - Valid JWT access token with ADMIN role

                    **Path Parameter:**
                    - id: user UUID

                    **Response:**
                    - id: user UUID
                    - username: user login name
                    - role: user role (USER or ADMIN)
                    - createdAt: account creation timestamp

                    **Error Responses:**
                    - 403 Forbidden: authenticated user lacks ADMIN role
                    - 401 Unauthorized: not authenticated
                    - 404 Not Found: user with specified ID doesn't exist
                    - 400 Bad Request: invalid UUID format
                    """,
            tags = {"users"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - requires ADMIN role",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ForbiddenError"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/UnauthorizedError"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/NotFoundError"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid UUID format",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/BadRequestError"))
            )
    })
    @GetMapping("/{id}")
    @RequireAuth(roles = {Role.ADMIN})
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(
                    description = "User UUID",
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    required = true
            )
            @PathVariable UUID id,
            HttpServletRequest request) {
        UserResponse response = userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id));
        return ResponseEntity.ok(response);
    }
}
