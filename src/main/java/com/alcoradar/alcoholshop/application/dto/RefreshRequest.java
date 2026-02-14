package com.alcoradar.alcoholshop.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank(message = "Refresh token must not be blank")
    String refreshToken
) {}
