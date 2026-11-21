package com.smartrecruit.controller;

import com.smartrecruit.dto.Dtos.*;
import com.smartrecruit.controller.JobController.JobStatusRequest;
import com.smartrecruit.service.AdminService;
import com.smartrecruit.service.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService admin;

    public AdminController(AdminService admin) {
        this.admin = admin;
    }

    @GetMapping("/stats")
    public AdminStats stats() {
        return admin.stats();
    }

    @GetMapping("/users")
    public List<UserDto> users() {
        return admin.users();
    }

    @PatchMapping("/users/{id}/enabled")
    public UserDto enabled(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                           @Valid @RequestBody EnabledRequest request) {
        return admin.setEnabled(CurrentUser.id(jwt), id, request.enabled());
    }

    @GetMapping("/jobs")
    public List<JobDto> jobs() {
        return admin.allJobs();
    }

    @PatchMapping("/jobs/{id}/status")
    public JobDto jobStatus(@PathVariable Long id, @Valid @RequestBody JobStatusRequest request) {
        return admin.setJobStatus(id, request.status());
    }
}
