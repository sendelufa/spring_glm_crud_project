package com.alcoradar.alcoholshop.interfaces.security;

import com.alcoradar.alcoholshop.application.service.SecurityService;
import com.alcoradar.alcoholshop.domain.exception.InvalidTokenException;
import com.alcoradar.alcoholshop.domain.model.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationAspectTest {

    @Mock
    private SecurityService securityService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Mock
    private HttpServletRequest request;

    private AuthenticationAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new AuthenticationAspect(securityService);
        // Setup default signature mocking
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("testMethod()");
    }

    @Test
    void authenticate_withValidTokenAndNoRoles_shouldProceed() throws Throwable {
        // Arrange
        Claims mockClaims = mock(Claims.class);
        UUID userId = UUID.randomUUID();
        String token = "valid-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(securityService.validateAccessToken(token)).thenReturn(mockClaims);
        when(mockClaims.get("userId", UUID.class)).thenReturn(userId);
        when(mockClaims.get("role", String.class)).thenReturn("USER");
        when(joinPoint.getArgs()).thenReturn(new Object[]{request});
        when(joinPoint.proceed()).thenReturn("result");

        RequireAuth requireAuth = mock(RequireAuth.class);
        when(requireAuth.roles()).thenReturn(new Role[]{});

        // Act
        Object result = aspect.authenticate(joinPoint, requireAuth);

        // Assert
        assertThat(result).isEqualTo("result");
        verify(joinPoint).proceed();
        verify(securityService).validateAccessToken(token);
    }

    @Test
    void authenticate_withValidTokenAndMatchingRole_shouldProceed() throws Throwable {
        // Arrange
        Claims mockClaims = mock(Claims.class);
        UUID userId = UUID.randomUUID();
        String token = "valid-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(securityService.validateAccessToken(token)).thenReturn(mockClaims);
        when(mockClaims.get("userId", UUID.class)).thenReturn(userId);
        when(mockClaims.get("role", String.class)).thenReturn("USER");
        when(joinPoint.getArgs()).thenReturn(new Object[]{request});
        when(joinPoint.proceed()).thenReturn("result");

        RequireAuth requireAuth = mock(RequireAuth.class);
        when(requireAuth.roles()).thenReturn(new Role[]{Role.USER});

        // Act
        Object result = aspect.authenticate(joinPoint, requireAuth);

        // Assert
        assertThat(result).isEqualTo("result");
        verify(joinPoint).proceed();
        verify(securityService).validateAccessToken(token);
    }

    @Test
    void authenticate_withMissingAuthorizationHeader_shouldThrowException() throws Throwable {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);
        when(joinPoint.getArgs()).thenReturn(new Object[]{request});

        RequireAuth requireAuth = mock(RequireAuth.class);
        when(requireAuth.roles()).thenReturn(new Role[]{});

        // Act & Assert
        assertThatThrownBy(() -> aspect.authenticate(joinPoint, requireAuth))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Missing or invalid Authorization header");
    }

    @Test
    void authenticate_withInvalidAuthorizationHeaderFormat_shouldThrowException() throws Throwable {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");
        when(joinPoint.getArgs()).thenReturn(new Object[]{request});

        RequireAuth requireAuth = mock(RequireAuth.class);
        when(requireAuth.roles()).thenReturn(new Role[]{});

        // Act & Assert
        assertThatThrownBy(() -> aspect.authenticate(joinPoint, requireAuth))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Missing or invalid Authorization header");
    }

    @Test
    void authenticate_withInvalidToken_shouldThrowException() throws Throwable {
        // Arrange
        String token = "invalid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(securityService.validateAccessToken(token))
                .thenThrow(new InvalidTokenException("Invalid token"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{request});

        RequireAuth requireAuth = mock(RequireAuth.class);
        when(requireAuth.roles()).thenReturn(new Role[]{});

        // Act & Assert
        assertThatThrownBy(() -> aspect.authenticate(joinPoint, requireAuth))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid token");
    }

    @Test
    void authenticate_withValidTokenButNonMatchingRole_shouldThrowException() throws Throwable {
        // Arrange
        Claims mockClaims = mock(Claims.class);
        UUID userId = UUID.randomUUID();
        String token = "valid-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(securityService.validateAccessToken(token)).thenReturn(mockClaims);
        when(mockClaims.get("userId", UUID.class)).thenReturn(userId);
        when(mockClaims.get("role", String.class)).thenReturn("ADMIN");
        when(joinPoint.getArgs()).thenReturn(new Object[]{request});

        RequireAuth requireAuth = mock(RequireAuth.class);
        when(requireAuth.roles()).thenReturn(new Role[]{Role.USER}); // Requires USER, token has ADMIN

        // Act & Assert
        assertThatThrownBy(() -> aspect.authenticate(joinPoint, requireAuth))
                .isInstanceOf(com.alcoradar.alcoholshop.domain.exception.AccessDeniedException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void authenticate_withMultipleRoles_oneMatching_shouldProceed() throws Throwable {
        // Arrange
        Claims mockClaims = mock(Claims.class);
        UUID userId = UUID.randomUUID();
        String token = "valid-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(securityService.validateAccessToken(token)).thenReturn(mockClaims);
        when(mockClaims.get("userId", UUID.class)).thenReturn(userId);
        when(mockClaims.get("role", String.class)).thenReturn("ADMIN");
        when(joinPoint.getArgs()).thenReturn(new Object[]{request});
        when(joinPoint.proceed()).thenReturn("result");

        RequireAuth requireAuth = mock(RequireAuth.class);
        when(requireAuth.roles()).thenReturn(new Role[]{Role.USER, Role.ADMIN}); // User has ADMIN which matches

        // Act
        Object result = aspect.authenticate(joinPoint, requireAuth);

        // Assert
        assertThat(result).isEqualTo("result");
        verify(joinPoint).proceed();
    }

    @Test
    void authenticate_withoutHttpServletRequestInArgs_shouldThrowException() throws Throwable {
        // Arrange
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        RequireAuth requireAuth = mock(RequireAuth.class);
        when(requireAuth.roles()).thenReturn(new Role[]{});

        // Act & Assert
        assertThatThrownBy(() -> aspect.authenticate(joinPoint, requireAuth))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HttpServletRequest not found in method signature");
    }
}
