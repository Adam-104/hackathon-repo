package com.smartrecruit.repository;

import com.smartrecruit.model.AnalysisType;
import com.smartrecruit.model.ResumeAnalysis;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    List<ResumeAnalysis> findTop30ByCandidateIdOrderByCreatedAtDesc(Long candidateId);

    Optional<ResumeAnalysis> findFirstByCandidateIdAndJobIdAndTypeOrderByCreatedAtDesc(
            Long candidateId, Long jobId, AnalysisType type);
}
