package com.alcoradar.alcoholshop.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class RoleTest {

    @Test
    void shouldHaveTwoRoles() {
        assertThat(Role.values()).hasSize(2);
    }

    @Test
    void shouldHaveUserRole() {
        assertThat(Role.valueOf("USER")).isEqualTo(Role.USER);
    }

    @Test
    void shouldHaveAdminRole() {
        assertThat(Role.valueOf("ADMIN")).isEqualTo(Role.ADMIN);
    }
}
