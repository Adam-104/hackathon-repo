package com.smartrecruit.controller;

import com.smartrecruit.dto.Dtos.*;
import com.smartrecruit.service.AiService;
import com.smartrecruit.service.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService ai;

    public AiController(AiService ai) {
        this.ai = ai;
    }

    @PostMapping("/analyze-resume")
    @PreAuthorize("hasAuthority('CANDIDATE')")
    public AnalysisDto analyze(@AuthenticationPrincipal Jwt jwt) {
        return ai.analyzeResume(CurrentUser.id(jwt));
    }

    @PostMapping("/match")
    @PreAuthorize("hasAuthority('CANDIDATE')")
    public AnalysisDto match(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody JobIdRequest request) {
        return ai.matchJob(CurrentUser.id(jwt), request.jobId());
    }

    @PostMapping("/interview-questions")
    @PreAuthorize("hasAuthority('CANDIDATE')")
    public AnalysisDto questions(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody InterviewRequest request) {
        return ai.interviewQuestions(CurrentUser.id(jwt), request.jobId(), request.count());
    }

    @PostMapping("/improve-resume")
    @PreAuthorize("hasAuthority('CANDIDATE')")
    public AnalysisDto improve(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody(required = false) ImproveRequest request) {
        return ai.improveResume(CurrentUser.id(jwt), request == null ? null : request.jobId());
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('CANDIDATE')")
    public List<AnalysisDto> history(@AuthenticationPrincipal Jwt jwt) {
        return ai.history(CurrentUser.id(jwt));
    }

    @PostMapping("/applications/{id}/screen")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public AnalysisDto screen(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ai.screen(CurrentUser.id(jwt), id);
    }

    @PostMapping("/applications/{id}/interview-questions")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public AnalysisDto recruiterQuestions(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                          @RequestParam(required = false) Integer count) {
        return ai.recruiterQuestions(CurrentUser.id(jwt), id, count);
    }
}
