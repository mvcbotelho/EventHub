package com.marcus.eventhub.admin;

import com.marcus.eventhub.admin.dto.AdminStatsResponse;
import com.marcus.eventhub.admin.dto.AdminUserResponse;
import com.marcus.eventhub.admin.dto.UpdateUserRoleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Administrative operations (admin role required)")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public List<AdminUserResponse> listUsers() {
        return adminService.listUsers();
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Update a user's role")
    public AdminUserResponse updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return adminService.updateUserRole(id, request);
    }

    @GetMapping("/stats")
    @Operation(summary = "Platform statistics")
    public AdminStatsResponse stats() {
        return adminService.stats();
    }

    @DeleteMapping("/events/{id}")
    @Operation(summary = "Soft-delete any event (admin override)")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        adminService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
