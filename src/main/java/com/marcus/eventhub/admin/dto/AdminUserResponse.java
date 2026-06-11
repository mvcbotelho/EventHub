package com.marcus.eventhub.admin.dto;

import com.marcus.eventhub.user.User;
import com.marcus.eventhub.user.UserRole;
import java.time.Instant;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        Instant createdAt
) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
