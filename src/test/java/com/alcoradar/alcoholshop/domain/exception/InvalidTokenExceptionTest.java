package com.alcoradar.alcoholshop.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class InvalidTokenExceptionTest {

    @Test
    void shouldHaveMessage() {
        InvalidTokenException exception = new InvalidTokenException("Malformed token");
        assertThat(exception.getMessage()).isEqualTo("Malformed token");
    }

    @Test
    void shouldBeAuthenticationException() {
        InvalidTokenException exception = new InvalidTokenException("Test");
        assertThat(exception).isInstanceOf(AuthenticationException.class);
    }
}
