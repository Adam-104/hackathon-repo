package com.smartrecruit.service;

import com.smartrecruit.dto.Dtos.*;
import com.smartrecruit.exception.ApiException;
import com.smartrecruit.model.Job;
import com.smartrecruit.model.JobStatus;
import com.smartrecruit.model.Role;
import com.smartrecruit.model.User;
import com.smartrecruit.repository.ApplicationRepository;
import com.smartrecruit.repository.JobRepository;
import com.smartrecruit.repository.ResumeAnalysisRepository;
import com.smartrecruit.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Platform administration: statistics, user accounts and job moderation. */
@Service
public class AdminService {

    private final UserRepository users;
    private final JobRepository jobs;
    private final ApplicationRepository applications;
    private final ResumeAnalysisRepository analyses;

    public AdminService(UserRepository users, JobRepository jobs, ApplicationRepository applications,
                        ResumeAnalysisRepository analyses) {
        this.users = users;
        this.jobs = jobs;
        this.applications = applications;
        this.analyses = analyses;
    }

    @Transactional(readOnly = true)
    public AdminStats stats() {
        long openJobs = jobs.findAll().stream().filter(j -> j.getStatus() == JobStatus.OPEN).count();
        return new AdminStats(users.count(), users.countByRole(Role.CANDIDATE), users.countByRole(Role.RECRUITER),
                users.countByRole(Role.ADMIN), users.countByEnabledFalse(), jobs.count(), openJobs,
                applications.count(), analyses.count());
    }

    @Transactional(readOnly = true)
    public List<UserDto> users() {
        return users.findAllByOrderByCreatedAtDesc().stream().map(UserDto::of).toList();
    }

    @Transactional
    public UserDto setEnabled(Long adminId, Long userId, boolean enabled) {
        User u = users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
        if (u.getRole() == Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Administrator accounts cannot be disabled here.");
        }
        u.setEnabled(enabled);
        return UserDto.of(users.save(u));
    }

    @Transactional(readOnly = true)
    public List<JobDto> allJobs() {
        return jobs.findAll().stream().sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(JobDto::of).toList();
    }

    @Transactional
    public JobDto setJobStatus(Long jobId, JobStatus status) {
        Job job = jobs.findById(jobId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job not found."));
        job.setStatus(status);
        return JobDto.of(jobs.save(job));
    }
}
