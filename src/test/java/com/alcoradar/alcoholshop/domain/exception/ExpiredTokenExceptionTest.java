package com.alcoradar.alcoholshop.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ExpiredTokenExceptionTest {

    @Test
    void shouldHaveMessage() {
        ExpiredTokenException exception = new ExpiredTokenException("Token expired");
        assertThat(exception.getMessage()).isEqualTo("Token expired");
    }

    @Test
    void shouldBeInvalidTokenException() {
        ExpiredTokenException exception = new ExpiredTokenException("Test");
        assertThat(exception).isInstanceOf(InvalidTokenException.class);
    }
}
