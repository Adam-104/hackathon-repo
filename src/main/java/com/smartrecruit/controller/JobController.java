package com.smartrecruit.controller;

import com.smartrecruit.dto.Dtos.*;
import com.smartrecruit.model.JobStatus;
import com.smartrecruit.service.ApplicationService;
import com.smartrecruit.service.CurrentUser;
import com.smartrecruit.service.JobService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    public record JobStatusRequest(@NotNull(message = "Status is required.") JobStatus status) {}

    private final JobService jobs;
    private final ApplicationService applications;

    public JobController(JobService jobs, ApplicationService applications) {
        this.jobs = jobs;
        this.applications = applications;
    }

    @GetMapping
    public List<JobDto> search(@RequestParam(required = false) String q) {
        return jobs.search(q);
    }

    @GetMapping("/{id}")
    public JobDto get(@PathVariable Long id) {
        return jobs.get(id);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public List<JobDto> mine(@AuthenticationPrincipal Jwt jwt) {
        return jobs.mine(CurrentUser.id(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('RECRUITER')")
    public JobDto create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody JobRequest request) {
        return jobs.create(CurrentUser.id(jwt), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public JobDto update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return jobs.update(CurrentUser.id(jwt), id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public JobDto status(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                         @Valid @RequestBody JobStatusRequest request) {
        return jobs.setStatus(CurrentUser.id(jwt), id, request.status());
    }

    @PostMapping("/{id}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CANDIDATE')")
    public ApplicationDto apply(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                @Valid @RequestBody(required = false) ApplyRequest request) {
        return applications.apply(CurrentUser.id(jwt), id, request);
    }

    @GetMapping("/{id}/applications")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public List<ApplicationDto> applicants(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return applications.forJob(CurrentUser.id(jwt), id);
    }
}
