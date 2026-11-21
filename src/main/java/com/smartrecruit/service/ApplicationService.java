package com.smartrecruit.service;

import com.smartrecruit.dto.Dtos.*;
import com.smartrecruit.exception.ApiException;
import com.smartrecruit.model.*;
import com.smartrecruit.repository.ApplicationRepository;
import com.smartrecruit.repository.ResumeAnalysisRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

    private final ApplicationRepository applications;
    private final ResumeAnalysisRepository analyses;
    private final CandidateService candidates;
    private final JobService jobs;

    public ApplicationService(ApplicationRepository applications, ResumeAnalysisRepository analyses,
                              CandidateService candidates, JobService jobs) {
        this.applications = applications;
        this.analyses = analyses;
        this.candidates = candidates;
        this.jobs = jobs;
    }

    @Transactional
    public ApplicationDto apply(Long userId, Long jobId, ApplyRequest req) {
        Candidate candidate = candidates.forUser(userId);
        Job job = jobs.find(jobId);
        if (job.getStatus() != JobStatus.OPEN) {
            throw new ApiException(HttpStatus.CONFLICT, "This job is closed.");
        }
        if (candidate.getResumeText() == null || candidate.getResumeText().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Upload your resume before applying.");
        }
        if (applications.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "You already applied to this job.");
        }
        Application app = new Application();
        app.setJob(job);
        app.setCandidate(candidate);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setCoverNote(req == null || req.coverNote() == null || req.coverNote().isBlank() ? null : req.coverNote().trim());
        app.setAppliedAt(Instant.now());
        // Reuse the candidate's latest AI match for this job, if they ran one.
        analyses.findFirstByCandidateIdAndJobIdAndTypeOrderByCreatedAtDesc(candidate.getId(), jobId, AnalysisType.MATCH)
                .ifPresent(m -> app.setMatchScore(m.getScore()));
        return ApplicationDto.of(applications.save(app), false);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> mine(Long userId) {
        Candidate candidate = candidates.forUser(userId);
        return applications.findByCandidateIdOrderByAppliedAtDesc(candidate.getId()).stream()
                .map(a -> ApplicationDto.of(a, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> forJob(Long recruiterId, Long jobId) {
        jobs.owned(recruiterId, jobId);
        return applications.findByJobIdOrderByAppliedAtDesc(jobId).stream()
                .map(a -> ApplicationDto.of(a, true)).toList();
    }

    @Transactional
    public ApplicationDto setStatus(Long recruiterId, Long applicationId, ApplicationStatus status) {
        Application app = owned(recruiterId, applicationId);
        app.setStatus(status);
        return ApplicationDto.of(applications.save(app), true);
    }

    /** Loads an application and makes sure its job belongs to this recruiter. */
    public Application owned(Long recruiterId, Long applicationId) {
        Application app = applications.findById(applicationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Application not found."));
        if (!app.getJob().getRecruiter().getId().equals(recruiterId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This application is for another recruiter's job.");
        }
        return app;
    }

    /** Everything the AI needs about one application, read inside a transaction. */
    public record ScreenData(Long applicationId, Long candidateId, Long jobId, String candidateName,
                             String headline, String skills, Integer years, String resume,
                             String jobTitle, String company, String jobDescription, String jobSkills,
                             String level) {}

    @Transactional(readOnly = true)
    public ScreenData screenData(Long recruiterId, Long applicationId) {
        Application a = owned(recruiterId, applicationId);
        Candidate c = a.getCandidate();
        Job j = a.getJob();
        return new ScreenData(a.getId(), c.getId(), j.getId(), c.getUser().getName(), c.getHeadline(),
                c.getSkills(), c.getExperienceYears(), c.getResumeText(), j.getTitle(), j.getCompany(),
                j.getDescription(), j.getRequiredSkills(), j.getExperienceLevel());
    }

    /** Stores a new AI match score on the application, if this candidate applied to the job. */
    @Transactional
    public void updateScoreIfApplied(Long candidateId, Long jobId, Integer score) {
        applications.findByJobIdAndCandidateId(jobId, candidateId).ifPresent(a -> {
            a.setMatchScore(score);
            applications.save(a);
        });
    }
}
