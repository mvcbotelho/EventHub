package com.marcus.eventhub.admin.dto;

import com.marcus.eventhub.user.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required")
        UserRole role
) {
}
