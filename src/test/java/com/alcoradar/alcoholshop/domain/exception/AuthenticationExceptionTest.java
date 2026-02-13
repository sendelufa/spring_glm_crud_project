package com.alcoradar.alcoholshop.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AuthenticationExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        AuthenticationException exception = new AuthenticationException("Test error") {};
        assertThat(exception.getMessage()).isEqualTo("Test error");
    }

    @Test
    void shouldBeDomainException() {
        AuthenticationException exception = new AuthenticationException("Test") {};
        assertThat(exception).isInstanceOf(DomainException.class);
    }
}
