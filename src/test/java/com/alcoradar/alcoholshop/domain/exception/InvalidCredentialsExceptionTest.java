package com.alcoradar.alcoholshop.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class InvalidCredentialsExceptionTest {

    @Test
    void shouldHaveMessage() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials");
        assertThat(exception.getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    void shouldBeAuthenticationException() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Test");
        assertThat(exception).isInstanceOf(AuthenticationException.class);
    }
}
