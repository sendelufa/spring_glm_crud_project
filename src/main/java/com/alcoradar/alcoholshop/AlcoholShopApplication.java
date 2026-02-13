package com.alcoradar.alcoholshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for AlcoholShop Service.
 *
 * <p>This service provides REST API for alcohol marketplace management
 * using Domain-Driven Design and Clean Architecture principles.</p>
 *
 * @author AlcoRadar Team
 * @version 1.0.0
 * @since 2025
 */
@SpringBootApplication
public class AlcoholShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlcoholShopApplication.class, args);
    }
}
