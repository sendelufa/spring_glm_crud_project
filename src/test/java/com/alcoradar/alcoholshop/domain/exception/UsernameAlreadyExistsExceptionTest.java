package com.alcoradar.alcoholshop.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class UsernameAlreadyExistsExceptionTest {

    @Test
    void shouldHaveMessageWithUsername() {
        UsernameAlreadyExistsException exception = new UsernameAlreadyExistsException("admin");
        assertThat(exception.getMessage()).contains("admin");
    }

    @Test
    void shouldBeDomainException() {
        UsernameAlreadyExistsException exception = new UsernameAlreadyExistsException("test");
        assertThat(exception).isInstanceOf(DomainException.class);
    }
}
