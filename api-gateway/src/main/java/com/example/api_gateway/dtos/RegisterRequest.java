package com.example.api_gateway.dtos;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 1, max = 255)
        String password,

        @NotBlank
        @Size(min = 1, max = 255)
        String name,

        @Min(18)
        int age,

        @Pattern(regexp = "ADMIN|USER")
        String role
) {}
