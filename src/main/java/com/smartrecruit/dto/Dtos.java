package com.smartrecruit.dto;

import com.smartrecruit.model.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;

/** All request and response shapes in one place. */
public final class Dtos {
    private Dtos() {}

    public record RegisterRequest(
            @NotBlank(message = "Name is required.") @Size(max = 120) String name,
            @NotBlank(message = "Email is required.") @Email(message = "Enter a valid email.") @Size(max = 160) String email,
            @NotBlank(message = "Password is required.")
            @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters.") String password,
            @NotNull(message = "Choose a role: RECRUITER or CANDIDATE.") Role role) {}

    public record LoginRequest(
            @NotBlank(message = "Email is required.") String email,
            @NotBlank(message = "Password is required.") String password) {}

    public record UserDto(Long id, String name, String email, Role role, boolean enabled, Instant createdAt) {
        public static UserDto of(User u) {
            return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isEnabled(), u.getCreatedAt());
        }
    }

    public record EnabledRequest(@NotNull(message = "enabled is required.") Boolean enabled) {}

    public record AdminStats(long users, long candidates, long recruiters, long admins, long disabledUsers,
                             long jobs, long openJobs, long applications, long aiResults) {}

    public record AuthResponse(String token, UserDto user) {}

    public record ProfileRequest(
            @Size(max = 30) String phone,
            @Size(max = 200) String headline,
            @Size(max = 1000) String skills,
            @Min(0) @Max(60) Integer experienceYears) {}

    public record CandidateDto(Long id, String name, String email, String phone, String headline, String skills,
                               Integer experienceYears, String resumeFileName, boolean hasResume, int resumeChars) {
        public static CandidateDto of(Candidate c) {
            String text = c.getResumeText();
            return new CandidateDto(c.getId(), c.getUser().getName(), c.getUser().getEmail(), c.getPhone(),
                    c.getHeadline(), c.getSkills(), c.getExperienceYears(), c.getResumeFileName(),
                    text != null && !text.isBlank(), text == null ? 0 : text.length());
        }
    }

    public record JobRequest(
            @NotBlank(message = "Title is required.") @Size(max = 160) String title,
            @NotBlank(message = "Company is required.") @Size(max = 160) String company,
            @Size(max = 160) String location,
            @NotBlank(message = "Description is required.") @Size(max = 8000) String description,
            @Size(max = 1000) String requiredSkills,
            @Size(max = 60) String experienceLevel) {}

    public record JobDto(Long id, String title, String company, String location, String description,
                         String requiredSkills, String experienceLevel, JobStatus status, Instant createdAt,
                         Long recruiterId, String recruiterName) {
        public static JobDto of(Job j) {
            return new JobDto(j.getId(), j.getTitle(), j.getCompany(), j.getLocation(), j.getDescription(),
                    j.getRequiredSkills(), j.getExperienceLevel(), j.getStatus(), j.getCreatedAt(),
                    j.getRecruiter().getId(), j.getRecruiter().getName());
        }
    }

    public record ApplyRequest(@Size(max = 2000) String coverNote) {}

    public record StatusRequest(@NotNull(message = "Status is required.") ApplicationStatus status) {}

    public record ApplicationDto(Long id, Long jobId, String jobTitle, String company, ApplicationStatus status,
                                 Integer matchScore, String coverNote, Instant appliedAt, CandidateDto candidate) {
        public static ApplicationDto of(Application a, boolean withCandidate) {
            return new ApplicationDto(a.getId(), a.getJob().getId(), a.getJob().getTitle(), a.getJob().getCompany(),
                    a.getStatus(), a.getMatchScore(), a.getCoverNote(), a.getAppliedAt(),
                    withCandidate ? CandidateDto.of(a.getCandidate()) : null);
        }
    }

    public record JobIdRequest(@NotNull(message = "jobId is required.") Long jobId) {}

    public record InterviewRequest(@NotNull(message = "jobId is required.") Long jobId,
                                   @Min(3) @Max(15) Integer count) {}

    public record ImproveRequest(Long jobId) {}

    /** resultJson is the model's JSON answer; the frontend parses it. */
    public record AnalysisDto(Long id, AnalysisType type, Integer score, Long jobId, String jobTitle,
                              String resultJson, Instant createdAt) {
        public static AnalysisDto of(ResumeAnalysis r) {
            return new AnalysisDto(r.getId(), r.getType(), r.getScore(),
                    r.getJob() == null ? null : r.getJob().getId(),
                    r.getJob() == null ? null : r.getJob().getTitle(), r.getResultJson(), r.getCreatedAt());
        }
    }

    public record JobStat(Long jobId, String title, long applications, Double averageScore) {}

    public record SkillStat(String skill, long jobs) {}

    public record AnalyticsDto(long totalJobs, long openJobs, long totalApplications, Double averageMatchScore,
                               java.util.Map<String, Long> byStatus, List<JobStat> perJob, List<SkillStat> topSkills) {}

    public record ErrorResponse(String message) {}

    public record MessageResponse(String message) {}

    public record VerifyEmailRequest(@NotBlank(message = "Token is required.") @Size(max = 200) String token) {}

    public record EmailRequest(@NotBlank(message = "Email is required.") @Email(message = "Enter a valid email.") @Size(max = 160) String email) {}
}
