package com.smartrecruit.controller;

import com.smartrecruit.dto.Dtos.*;
import com.smartrecruit.service.CandidateService;
import com.smartrecruit.service.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidate")
@PreAuthorize("hasAuthority('CANDIDATE')")
public class CandidateController {

    private final CandidateService candidates;

    public CandidateController(CandidateService candidates) {
        this.candidates = candidates;
    }

    @GetMapping("/profile")
    public CandidateDto profile(@AuthenticationPrincipal Jwt jwt) {
        return candidates.profile(CurrentUser.id(jwt));
    }

    @PutMapping("/profile")
    public CandidateDto update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProfileRequest request) {
        return candidates.update(CurrentUser.id(jwt), request);
    }

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CandidateDto resume(@AuthenticationPrincipal Jwt jwt, @RequestParam("file") MultipartFile file) {
        return candidates.uploadResume(CurrentUser.id(jwt), file);
    }
}
