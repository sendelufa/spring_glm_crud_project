package com.alcoradar.alcoholshop.interfaces.rest;

import com.alcoradar.alcoholshop.application.dto.CreateUserRequest;
import com.alcoradar.alcoholshop.application.dto.LoginRequest;
import com.alcoradar.alcoholshop.application.dto.LoginResponse;
import com.alcoradar.alcoholshop.application.dto.UserResponse;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for UserController.
 *
 * <p>Uses MockMvc for HTTP requests and Testcontainers for PostgreSQL database.
 * Tests user management endpoints including user creation and user lookup by ID.</p>
 *
 * <p>Test coverage:</p>
 * <ul>
 *   <li>POST /api/users - creating users as admin and non-admin</li>
 *   <li>GET /api/users/{id} - retrieving users as admin and non-admin</li>
 *   <li>Role-based access control verification</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {

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
    private SecurityService securityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    private User adminUser;
    private User regularUser;
    private String adminToken;
    private String userToken;
    private String adminPassword = "AdminPass123!";
    private String userPassword = "UserPass123!";

    @BeforeEach
    void setUp() throws Exception {
        // Create admin user
        String adminHashedPassword = securityService.hashPassword(adminPassword);
        adminUser = User.create("admin", adminHashedPassword, Role.ADMIN);
        adminUser = userRepository.save(adminUser);

        // Create regular user
        String userHashedPassword = securityService.hashPassword(userPassword);
        regularUser = User.create("user", userHashedPassword, Role.USER);
        regularUser = userRepository.save(regularUser);

        // Login as admin to get token
        adminToken = loginAndGetToken(adminUser.getUsername(), adminPassword);

        // Login as regular user to get token
        userToken = loginAndGetToken(regularUser.getUsername(), userPassword);
    }

    @AfterEach
    void tearDown() {
        // Clean up database
        springDataUserRepository.deleteAll();
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);
        return loginResponse.accessToken();
    }

    @Test
    @DisplayName("Should create user as admin and return 200 OK")
    void shouldCreateUserAsAdmin() throws Exception {
        // Given: create user request with valid data
        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "NewUser123!",
                Role.USER
        );

        // When: send create user request as admin
        MvcResult result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value(Role.USER.name()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn();

        // Then: verify user was created in database
        String response = result.getResponse().getContentAsString();
        UserResponse userResponse = objectMapper.readValue(response, UserResponse.class);
        assertThat(userRepository.findById(userResponse.id())).isPresent();
    }

    @Test
    @DisplayName("Should create admin user as admin and return 200 OK")
    void shouldCreateAdminUserAsAdmin() throws Exception {
        // Given: create admin request
        CreateUserRequest request = new CreateUserRequest(
                "newadmin",
                "NewAdmin123!",
                Role.ADMIN
        );

        // When: send create admin request as admin
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newadmin"))
                .andExpect(jsonPath("$.role").value(Role.ADMIN.name()));
    }

    @Test
    @DisplayName("Should return 403 Forbidden when regular user tries to create user")
    void shouldReturnForbiddenWhenRegularUserTriesToCreateUser() throws Exception {
        // Given: create user request
        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "NewUser123!",
                Role.USER
        );

        // When: send create user request as regular user
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when creating user without token")
    void shouldReturnUnauthorizedWhenCreatingUserWithoutToken() throws Exception {
        // Given: create user request
        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "NewUser123!",
                Role.USER
        );

        // When: send create user request without authorization
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 409 Conflict when creating user with existing username")
    void shouldReturnConflictWhenCreatingUserWithExistingUsername() throws Exception {
        // Given: create user request with existing username
        CreateUserRequest request = new CreateUserRequest(
                adminUser.getUsername(), // username already exists
                "NewUser123!",
                Role.USER
        );

        // When: send create user request as admin
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when creating user with invalid password")
    void shouldReturnBadRequestForInvalidPassword() throws Exception {
        // Given: create user request with weak password (missing uppercase)
        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "weakpassword",
                Role.USER
        );

        // When: send create user request as admin
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when creating user with short username")
    void shouldReturnBadRequestForShortUsername() throws Exception {
        // Given: create user request with short username
        CreateUserRequest request = new CreateUserRequest(
                "ab", // less than 3 characters
                "ValidPass123!",
                Role.USER
        );

        // When: send create user request as admin
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when creating user with blank username")
    void shouldReturnBadRequestForBlankUsername() throws Exception {
        // Given: create user request with blank username
        CreateUserRequest request = new CreateUserRequest(
                "",
                "ValidPass123!",
                Role.USER
        );

        // When: send create user request as admin
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when creating user with blank password")
    void shouldReturnBadRequestForBlankPassword() throws Exception {
        // Given: create user request with blank password
        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "",
                Role.USER
        );

        // When: send create user request as admin
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get user by ID as admin and return 200 OK")
    void shouldGetUserByIdAsAdmin() throws Exception {
        // When: send get user by ID request as admin
        mockMvc.perform(get("/api/users/{id}", regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(regularUser.getId().toString()))
                .andExpect(jsonPath("$.username").value(regularUser.getUsername()))
                .andExpect(jsonPath("$.role").value(Role.USER.name()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("Should get admin user by ID as admin and return 200 OK")
    void shouldGetAdminUserByIdAsAdmin() throws Exception {
        // When: send get admin user by ID request as admin
        mockMvc.perform(get("/api/users/{id}", adminUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(adminUser.getId().toString()))
                .andExpect(jsonPath("$.username").value(adminUser.getUsername()))
                .andExpect(jsonPath("$.role").value(Role.ADMIN.name()));
    }

    @Test
    @DisplayName("Should return 403 Forbidden when regular user tries to get user by ID")
    void shouldReturnForbiddenWhenRegularUserTriesToGetUserById() throws Exception {
        // When: send get user by ID request as regular user
        mockMvc.perform(get("/api/users/{id}", regularUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when getting user by ID without token")
    void shouldReturnUnauthorizedWhenGettingUserByIdWithoutToken() throws Exception {
        // When: send get user by ID request without authorization
        mockMvc.perform(get("/api/users/{id}", regularUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 Not Found when getting non-existent user by ID")
    void shouldReturnNotFoundForNonExistentUser() throws Exception {
        // Given: non-existent user ID
        UUID nonExistentId = UUID.randomUUID();

        // When: send get user by ID request as admin
        mockMvc.perform(get("/api/users/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when getting user by ID with invalid UUID")
    void shouldReturnBadRequestForInvalidUUID() throws Exception {
        // When: send get user by ID request with invalid UUID
        mockMvc.perform(get("/api/users/{id}", "invalid-uuid")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create user and then retrieve by ID as admin")
    void shouldCreateUserAndRetrieveById() throws Exception {
        // Given: create user request
        CreateUserRequest request = new CreateUserRequest(
                "testuser",
                "TestUser123!",
                Role.USER
        );

        // When: create user as admin
        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Then: extract created user ID
        String createResponse = createResult.getResponse().getContentAsString();
        UserResponse userResponse = objectMapper.readValue(createResponse, UserResponse.class);
        UUID createdUserId = userResponse.id();

        // And: retrieve user by ID
        mockMvc.perform(get("/api/users/{id}", createdUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUserId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("Should return 403 Forbidden when regular user tries to access admin endpoint")
    void shouldReturnForbiddenForRegularUserAccessingAdminEndpoint() throws Exception {
        // When: regular user tries to access another user's data
        mockMvc.perform(get("/api/users/{id}", adminUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when regular user tries to create admin user")
    void shouldReturnForbiddenForRegularUserCreatingAdminUser() throws Exception {
        // Given: create admin request
        CreateUserRequest request = new CreateUserRequest(
                "newadmin",
                "NewAdmin123!",
                Role.ADMIN
        );

        // When: regular user tries to create admin
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when accessing user endpoint with expired token")
    void shouldReturnUnauthorizedForExpiredToken() throws Exception {
        // Given: create token that will expire soon
        String expiredToken = securityService.generateAccessToken(regularUser, 1); // 1 second

        // Wait for token to expire
        Thread.sleep(1100); // Sleep slightly longer than 1 second

        // When: try to get user by ID with expired token
        mockMvc.perform(get("/api/users/{id}", regularUser.getId())
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}
