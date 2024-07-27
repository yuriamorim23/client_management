package com.example.client.management.dto;

import com.example.client.management.enums.UserRole;

import jakarta.validation.constraints.NotNull;

public record RegisterDto(@NotNull String email,@NotNull String password, @NotNull UserRole role ) {
    
}
