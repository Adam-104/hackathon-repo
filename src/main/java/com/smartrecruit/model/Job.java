package com.smartrecruit.model;

import jakarta.persistence.*;
import java.time.Instant;


@Entity
@Table(name = "jobs")
public class Job {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "recruiter_id", nullable = false)
    private User recruiter;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 160)
    private String company;

    @Column(length = 160)
    private String location;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 1000)
    private String requiredSkills;

    @Column(length = 60)
    private String experienceLevel;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private JobStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    public Job() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getRecruiter() { return recruiter; }
    public void setRecruiter(User recruiter) { this.recruiter = recruiter; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

}
