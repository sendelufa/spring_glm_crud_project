package com.alcoradar.alcoholshop.interfaces.rest;

import com.alcoradar.alcoholshop.application.dto.*;
import com.alcoradar.alcoholshop.application.usecase.AuthenticationUseCase;
import com.alcoradar.alcoholshop.application.service.SecurityService;
import com.alcoradar.alcoholshop.domain.model.Role;
import com.alcoradar.alcoholshop.domain.model.User;
import com.alcoradar.alcoholshop.domain.repository.UserRepository;
import com.alcoradar.alcoholshop.infrastructure.persistence.repository.SpringDataUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for AuthenticationController.
 *
 * <p>Uses MockMvc for HTTP requests and Testcontainers for PostgreSQL database.
 * Tests authentication endpoints including login, token refresh, and current user retrieval.</p>
 *
 * <p>Test coverage:</p>
 * <ul>
 *   <li>POST /api/auth/login - successful and failed login attempts</li>
 *   <li>POST /api/auth/refresh - successful and failed token refresh</li>
 *   <li>GET /api/auth/me - retrieving current user with and without authentication</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class AuthenticationControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationUseCase authenticationUseCase;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    private User testUser;
    private String rawPassword = "TestPassword123!";

    @BeforeEach
    void setUp() {
        // Create a test user
        String hashedPassword = securityService.hashPassword(rawPassword);
        testUser = User.create("testuser", hashedPassword, Role.USER);
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        // Clean up database
        springDataUserRepository.deleteAll();
    }

    @Test
    @DisplayName("Should login with valid credentials and return 200 OK with tokens")
    void shouldLoginWithValidCredentials() throws Exception {
        // Given: valid login request
        LoginRequest request = new LoginRequest(testUser.getUsername(), rawPassword);

        // When: send login request
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.user.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.user.role").value(Role.USER.name()))
                .andExpect(jsonPath("$.user.createdAt").isNotEmpty())
                .andReturn();

        // Then: verify response structure
        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("\"accessToken\":");
        assertThat(response).contains("\"refreshToken\":");
        assertThat(response).contains("\"user\":");
    }

    @Test
    @DisplayName("Should return 401 Unauthorized for login with invalid username")
    void shouldReturnUnauthorizedForInvalidUsername() throws Exception {
        // Given: login request with non-existent username
        LoginRequest request = new LoginRequest("nonexistentuser", rawPassword);

        // When: send login request
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized for login with invalid password")
    void shouldReturnUnauthorizedForInvalidPassword() throws Exception {
        // Given: login request with wrong password
        LoginRequest request = new LoginRequest(testUser.getUsername(), "WrongPassword123!");

        // When: send login request
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 Bad Request for login with blank username")
    void shouldReturnBadRequestForBlankUsername() throws Exception {
        // Given: login request with blank username
        LoginRequest request = new LoginRequest("", rawPassword);

        // When: send login request
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request for login with blank password")
    void shouldReturnBadRequestForBlankPassword() throws Exception {
        // Given: login request with blank password
        LoginRequest request = new LoginRequest(testUser.getUsername(), "");

        // When: send login request
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request for login with short username")
    void shouldReturnBadRequestForShortUsername() throws Exception {
        // Given: login request with username shorter than 3 characters
        LoginRequest request = new LoginRequest("ab", rawPassword);

        // When: send login request
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should refresh token with valid refresh token and return 200 OK")
    void shouldRefreshTokenWithValidRefreshToken() throws Exception {
        // Given: login to get refresh token
        LoginRequest loginRequest = new LoginRequest(testUser.getUsername(), rawPassword);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);

        // When: send refresh request
        RefreshRequest refreshRequest = new RefreshRequest(loginResponse.refreshToken());
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized for refresh with invalid token")
    void shouldReturnUnauthorizedForInvalidRefreshToken() throws Exception {
        // Given: invalid refresh token
        RefreshRequest request = new RefreshRequest("invalid.refresh.token");

        // When: send refresh request
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 Bad Request for refresh with blank token")
    void shouldReturnBadRequestForBlankRefreshToken() throws Exception {
        // Given: refresh request with blank token
        RefreshRequest request = new RefreshRequest("");

        // When: send refresh request
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get current user with valid access token and return 200 OK")
    void shouldGetCurrentUserWithValidAccessToken() throws Exception {
        // Given: login to get access token
        LoginRequest loginRequest = new LoginRequest(testUser.getUsername(), rawPassword);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);

        // When: send get current user request with access token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + loginResponse.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.role").value(Role.USER.name()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized for get current user without token")
    void shouldReturnUnauthorizedForGetCurrentUserWithoutToken() throws Exception {
        // When: send get current user request without authorization header
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized for get current user with invalid token")
    void shouldReturnUnauthorizedForGetCurrentUserWithInvalidToken() throws Exception {
        // When: send get current user request with invalid token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.access.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized for get current user with malformed token")
    void shouldReturnUnauthorizedForGetCurrentUserWithMalformedToken() throws Exception {
        // When: send get current user request with malformed authorization header
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "InvalidFormat token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized for get current user with expired token")
    void shouldReturnUnauthorizedForGetCurrentUserWithExpiredToken() throws Exception {
        // Given: create an expired access token (using very short expiration for testing)
        // We need to wait for it to expire, so use 1 second and then Thread.sleep
        String expiredToken = securityService.generateAccessToken(testUser, 1); // 1 second

        // Wait for token to expire
        Thread.sleep(1100); // Sleep slightly longer than 1 second

        // When: send get current user request with expired token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should login and access protected endpoint with token")
    void shouldLoginAndAccessProtectedEndpoint() throws Exception {
        // Given: login to get access token
        LoginRequest loginRequest = new LoginRequest(testUser.getUsername(), rawPassword);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);

        // When: access protected endpoint with valid token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + loginResponse.accessToken()))
                .andExpect(status().isOk());

        // Then: verify token works as expected
        assertThat(loginResponse.accessToken()).isNotEmpty();
        assertThat(loginResponse.refreshToken()).isNotEmpty();
    }

    @Test
    @DisplayName("Should refresh token and access protected endpoint with new token")
    void shouldRefreshTokenAndAccessProtectedEndpoint() throws Exception {
        // Given: login to get tokens
        LoginRequest loginRequest = new LoginRequest(testUser.getUsername(), rawPassword);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);

        // When: refresh access token
        RefreshRequest refreshRequest = new RefreshRequest(loginResponse.refreshToken());
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andReturn();

        String refreshResponse = refreshResult.getResponse().getContentAsString();
        RefreshResponse refreshResponseObj = objectMapper.readValue(refreshResponse, RefreshResponse.class);

        // Then: verify new token works for protected endpoint
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + refreshResponseObj.accessToken()))
                .andExpect(status().isOk());
    }
}
