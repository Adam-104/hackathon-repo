package com.smartrecruit.service;

import com.smartrecruit.dto.Dtos.AnalysisDto;
import com.smartrecruit.model.AnalysisType;
import com.smartrecruit.model.ResumeAnalysis;
import com.smartrecruit.repository.CandidateRepository;
import com.smartrecruit.repository.JobRepository;
import com.smartrecruit.repository.ResumeAnalysisRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Saves and lists AI results (table resume_analysis). */
@Service
public class AnalysisStore {

    private final ResumeAnalysisRepository analyses;
    private final CandidateRepository candidates;
    private final JobRepository jobs;

    public AnalysisStore(ResumeAnalysisRepository analyses, CandidateRepository candidates, JobRepository jobs) {
        this.analyses = analyses;
        this.candidates = candidates;
        this.jobs = jobs;
    }

    @Transactional
    public AnalysisDto save(Long candidateId, Long jobId, AnalysisType type, Integer score, String json) {
        ResumeAnalysis r = new ResumeAnalysis();
        r.setCandidate(candidates.getReferenceById(candidateId));
        r.setJob(jobId == null ? null : jobs.getReferenceById(jobId));
        r.setType(type);
        r.setScore(score);
        r.setResultJson(json);
        r.setCreatedAt(Instant.now());
        return AnalysisDto.of(analyses.save(r));
    }

    @Transactional(readOnly = true)
    public List<AnalysisDto> history(Long candidateId) {
        return analyses.findTop30ByCandidateIdOrderByCreatedAtDesc(candidateId).stream().map(AnalysisDto::of).toList();
    }
}
