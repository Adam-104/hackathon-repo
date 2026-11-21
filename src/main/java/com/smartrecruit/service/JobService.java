package com.smartrecruit.service;

import com.smartrecruit.dto.Dtos.*;
import com.smartrecruit.exception.ApiException;
import com.smartrecruit.model.Job;
import com.smartrecruit.model.JobStatus;
import com.smartrecruit.model.User;
import com.smartrecruit.repository.JobRepository;
import com.smartrecruit.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {

    private final JobRepository jobs;
    private final UserRepository users;

    public JobService(JobRepository jobs, UserRepository users) {
        this.jobs = jobs;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<JobDto> search(String q) {
        return jobs.search(JobStatus.OPEN, q == null ? "" : q.trim()).stream().map(JobDto::of).toList();
    }

    @Transactional(readOnly = true)
    public JobDto get(Long id) {
        return JobDto.of(find(id));
    }

    @Transactional(readOnly = true)
    public List<JobDto> mine(Long recruiterId) {
        return jobs.findByRecruiterIdOrderByCreatedAtDesc(recruiterId).stream().map(JobDto::of).toList();
    }

    @Transactional
    public JobDto create(Long recruiterId, JobRequest req) {
        User recruiter = users.findById(recruiterId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account not found."));
        Job job = new Job();
        job.setRecruiter(recruiter);
        job.setStatus(JobStatus.OPEN);
        job.setCreatedAt(Instant.now());
        apply(job, req);
        return JobDto.of(jobs.save(job));
    }

    @Transactional
    public JobDto update(Long recruiterId, Long id, JobRequest req) {
        Job job = owned(recruiterId, id);
        apply(job, req);
        return JobDto.of(jobs.save(job));
    }

    @Transactional
    public JobDto setStatus(Long recruiterId, Long id, JobStatus status) {
        Job job = owned(recruiterId, id);
        job.setStatus(status);
        return JobDto.of(jobs.save(job));
    }

    public Job find(Long id) {
        return jobs.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job not found."));
    }

    /** Loads a job and makes sure it belongs to this recruiter. */
    public Job owned(Long recruiterId, Long id) {
        Job job = find(id);
        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This job belongs to another recruiter.");
        }
        return job;
    }

    private static void apply(Job job, JobRequest req) {
        job.setTitle(req.title().trim());
        job.setCompany(req.company().trim());
        job.setLocation(req.location() == null || req.location().isBlank() ? null : req.location().trim());
        job.setDescription(req.description().trim());
        job.setRequiredSkills(req.requiredSkills() == null || req.requiredSkills().isBlank() ? null : req.requiredSkills().trim());
        job.setExperienceLevel(req.experienceLevel() == null || req.experienceLevel().isBlank() ? null : req.experienceLevel().trim());
    }
}
