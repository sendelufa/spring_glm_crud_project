package com.alcoradar.alcoholshop.interfaces.rest;

import com.alcoradar.alcoholshop.application.dto.LoginRequest;
import com.alcoradar.alcoholshop.application.dto.LoginResponse;
import com.alcoradar.alcoholshop.application.dto.RefreshRequest;
import com.alcoradar.alcoholshop.application.dto.RefreshResponse;
import com.alcoradar.alcoholshop.application.dto.UserResponse;
import com.alcoradar.alcoholshop.application.usecase.AuthenticationUseCase;
import com.alcoradar.alcoholshop.domain.exception.UserNotFoundException;
import com.alcoradar.alcoholshop.domain.repository.UserRepository;
import com.alcoradar.alcoholshop.interfaces.security.RequireAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for authentication operations.
 *
 * <p>This controller provides authentication endpoints including login, token refresh,
 * and current user information. Follows Clean Architecture principles as part of the
 * interfaces layer (REST adapter).</p>
 *
 * <p><b>Endpoints:</b></p>
 * <ul>
 *   <li>POST /api/auth/login - authenticate user and receive tokens</li>
 *   <li>POST /api/auth/refresh - refresh access token using refresh token</li>
 *   <li>GET /api/auth/me - get currently authenticated user information</li>
 * </ul>
 *
 * <p><b>Security:</b></p>
 * <ul>
 *   <li>/api/auth/login - public endpoint, no authentication required</li>
 *   <li>/api/auth/refresh - public endpoint, valid refresh token required in request body</li>
 *   <li>/api/auth/me - requires valid JWT access token in Authorization header</li>
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
 * @see LoginRequest
 * @see LoginResponse
 * @see RefreshRequest
 * @see RefreshResponse
 * @see UserResponse
 */
@Tag(name = "authentication", description = "Authentication and authorization endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationUseCase authenticationUseCase;
    private final UserRepository userRepository;

    /**
     * Authenticates a user with username and password.
     *
     * <p>Accepts {@link LoginRequest} with credentials, validates them, and returns
     * JWT tokens (access + refresh) along with user information.</p>
     *
     * <p>Access token expires in 15 minutes (900 seconds).
     * Refresh token expires in 7 days (604800 seconds).</p>
     *
     * <p>Upon successful authentication, returns HTTP 200 OK with tokens.
     * Invalid credentials result in HTTP 401 Unauthorized.</p>
     *
     * @param request DTO containing username and password
     * @return DTO with access token, refresh token, and user information
     */
    @Operation(
            summary = "Authenticate user",
            description = """
                    Authenticate with username and password to receive JWT tokens.

                    **Request Body:**
                    - username: user login name (3-50 characters)
                    - password: user password

                    **Response:**
                    - accessToken: JWT token for API access (expires in 15 minutes)
                    - refreshToken: JWT token for obtaining new access tokens (expires in 7 days)
                    - user: user information (id, username, role, createdAt)

                    **Error Responses:**
                    - 401 Unauthorized: invalid username or password
                    - 400 Bad Request: validation errors
                    """,
            tags = {"authentication"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful authentication",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/UnauthorizedError"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationError"))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationUseCase.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * <p>Accepts a refresh token and returns a new access token without requiring
     * the user to re-authenticate with credentials.</p>
     *
     * <p>The refresh token must be valid and not expired. After successful refresh,
     * the client receives a new access token valid for 15 minutes.</p>
     *
     * @param request DTO containing the refresh token
     * @return DTO with new access token
     */
    @Operation(
            summary = "Refresh access token",
            description = """
                    Get a new access token using a valid refresh token.

                    **Request Body:**
                    - refreshToken: valid refresh token received from login

                    **Response:**
                    - accessToken: new JWT token for API access (expires in 15 minutes)

                    **Error Responses:**
                    - 401 Unauthorized: invalid or expired refresh token
                    - 400 Bad Request: validation errors
                    """,
            tags = {"authentication"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RefreshResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/UnauthorizedError"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationError"))
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = authenticationUseCase.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets information about the currently authenticated user.
     *
     * <p>This endpoint requires a valid JWT access token in the Authorization header.
     * The token is validated by {@link com.alcoradar.alcoholshop.interfaces.security.AuthenticationAspect}
     * before the method executes.</p>
     *
     * <p>The user ID is extracted from the JWT token claims and stored as a request attribute
     * by the authentication aspect.</p>
     *
     * @param request HTTP servlet request containing userId attribute set by authentication aspect
     * @return DTO with current user information
     */
    @Operation(
            summary = "Get current user",
            description = """
                    Get information about the currently authenticated user.

                    **Requires:**
                    - Valid JWT access token in Authorization header (Bearer token)

                    **Response:**
                    - id: user UUID
                    - username: user login name
                    - role: user role (USER or ADMIN)
                    - createdAt: account creation timestamp

                    **Error Responses:**
                    - 401 Unauthorized: missing, invalid, or expired access token
                    - 404 Not Found: user not found (should not happen with valid token)
                    """,
            tags = {"authentication"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User information retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
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
            )
    })
    @GetMapping("/me")
    @RequireAuth
    public ResponseEntity<UserResponse> getCurrentUser(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        UserResponse response = userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return ResponseEntity.ok(response);
    }
}
