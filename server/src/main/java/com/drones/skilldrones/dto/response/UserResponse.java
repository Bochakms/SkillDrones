package com.drones.skilldrones.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
    Long userId,
    String role,
    LocalDateTime createdAt,
    LocalDateTime lastLogin
) {}
