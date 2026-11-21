package com.smartrecruit.service;

import com.smartrecruit.dto.Dtos.*;
import com.smartrecruit.model.ApplicationStatus;
import com.smartrecruit.model.Job;
import com.smartrecruit.model.JobStatus;
import com.smartrecruit.repository.ApplicationRepository;
import com.smartrecruit.repository.JobRepository;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private final JobRepository jobs;
    private final ApplicationRepository applications;

    public AnalyticsService(JobRepository jobs, ApplicationRepository applications) {
        this.jobs = jobs;
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public AnalyticsDto forRecruiter(Long recruiterId) {
        List<Job> mine = jobs.findByRecruiterIdOrderByCreatedAtDesc(recruiterId);
        long open = mine.stream().filter(j -> j.getStatus() == JobStatus.OPEN).count();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ApplicationStatus s : ApplicationStatus.values()) byStatus.put(s.name(), 0L);
        for (Object[] row : applications.countByStatusForRecruiter(recruiterId)) {
            byStatus.put(((ApplicationStatus) row[0]).name(), (Long) row[1]);
        }

        List<JobStat> perJob = new ArrayList<>();
        for (Object[] row : applications.perJobForRecruiter(recruiterId)) {
            Double avg = row[3] == null ? null : Math.round(((Number) row[3]).doubleValue() * 10) / 10.0;
            perJob.add(new JobStat((Long) row[0], (String) row[1], (Long) row[2], avg));
        }

        // Skills requested most often across this recruiter's postings.
        Map<String, Long> skills = new HashMap<>();
        for (Job j : mine) {
            if (j.getRequiredSkills() == null) continue;
            Set<String> seen = new HashSet<>();
            for (String raw : j.getRequiredSkills().split(",")) {
                String s = raw.trim();
                if (!s.isEmpty() && seen.add(s.toLowerCase())) skills.merge(s, 1L, Long::sum);
            }
        }
        List<SkillStat> top = skills.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(8).map(e -> new SkillStat(e.getKey(), e.getValue())).toList();

        Double avgScore = applications.averageScoreForRecruiter(recruiterId);
        return new AnalyticsDto(mine.size(), open, applications.countForRecruiter(recruiterId),
                avgScore == null ? null : Math.round(avgScore * 10) / 10.0, byStatus, perJob, top);
    }
}
