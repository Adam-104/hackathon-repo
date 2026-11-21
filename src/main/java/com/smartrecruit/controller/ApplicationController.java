package com.smartrecruit.controller;

import com.smartrecruit.dto.Dtos.*;
import com.smartrecruit.service.ApplicationService;
import com.smartrecruit.service.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applications;

    public ApplicationController(ApplicationService applications) {
        this.applications = applications;
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('CANDIDATE')")
    public List<ApplicationDto> mine(@AuthenticationPrincipal Jwt jwt) {
        return applications.mine(CurrentUser.id(jwt));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ApplicationDto status(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                 @Valid @RequestBody StatusRequest request) {
        return applications.setStatus(CurrentUser.id(jwt), id, request.status());
    }
}
