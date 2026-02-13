package com.alcoradar.alcoholshop.domain.exception;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class UserNotFoundExceptionTest {

    @Test
    void shouldHaveMessageWithId() {
        UUID id = UUID.randomUUID();
        UserNotFoundException exception = new UserNotFoundException(id);
        assertThat(exception.getMessage()).contains(id.toString());
    }

    @Test
    void shouldBeDomainException() {
        UUID id = UUID.randomUUID();
        UserNotFoundException exception = new UserNotFoundException(id);
        assertThat(exception).isInstanceOf(DomainException.class);
    }
}
