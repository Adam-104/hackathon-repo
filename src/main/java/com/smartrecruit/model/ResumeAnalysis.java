package com.smartrecruit.model;

import jakarta.persistence.*;
import java.time.Instant;


@Entity
@Table(name = "resume_analysis")
public class ResumeAnalysis {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "job_id")
    private Job job;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AnalysisType type;

    private Integer score;

    @Column(columnDefinition = "LONGTEXT")
    private String resultJson;

    @Column(nullable = false)
    private Instant createdAt;

    public ResumeAnalysis() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public AnalysisType getType() { return type; }
    public void setType(AnalysisType type) { this.type = type; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

}
