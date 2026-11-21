package com.smartrecruit.controller;

import com.smartrecruit.dto.Dtos.AnalyticsDto;
import com.smartrecruit.service.AnalyticsService;
import com.smartrecruit.service.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analytics;

    public AnalyticsController(AnalyticsService analytics) {
        this.analytics = analytics;
    }

    @GetMapping("/recruiter")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public AnalyticsDto recruiter(@AuthenticationPrincipal Jwt jwt) {
        return analytics.forRecruiter(CurrentUser.id(jwt));
    }
}
