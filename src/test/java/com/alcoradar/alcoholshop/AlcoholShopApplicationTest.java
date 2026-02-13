package com.alcoradar.alcoholshop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic application context test.
 *
 * <p>This test verifies that the Spring application context loads successfully.</p>
 *
 * @author AlcoRadar Team
 * @version 1.0.0
 * @since 2025
 */
@SpringBootTest
class AlcoholShopApplicationTest {

    @Test
    void contextLoads() {
        // If this test passes, the Spring context loaded successfully
        assertThat(true).isTrue();
    }
}
