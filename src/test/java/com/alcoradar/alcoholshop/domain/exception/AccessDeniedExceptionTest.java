package com.alcoradar.alcoholshop.domain.exception;

import com.alcoradar.alcoholshop.domain.model.Role;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AccessDeniedExceptionTest {

    @Test
    void shouldHaveMessageWithRoles() {
        AccessDeniedException exception = new AccessDeniedException(Role.ADMIN, Role.USER);
        assertThat(exception.getMessage()).contains("ADMIN");
        assertThat(exception.getMessage()).contains("USER");
    }

    @Test
    void shouldHaveRequiredRole() {
        AccessDeniedException exception = new AccessDeniedException(Role.ADMIN, Role.USER);
        assertThat(exception.getRequiredRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldHaveUserRole() {
        AccessDeniedException exception = new AccessDeniedException(Role.ADMIN, Role.USER);
        assertThat(exception.getUserRole()).isEqualTo(Role.USER);
    }
}
