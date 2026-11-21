package com.smartrecruit.repository;

import com.smartrecruit.model.Application;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    Optional<Application> findByJobIdAndCandidateId(Long jobId, Long candidateId);

    List<Application> findByCandidateIdOrderByAppliedAtDesc(Long candidateId);

    List<Application> findByJobIdOrderByAppliedAtDesc(Long jobId);

    @Query("select count(a) from Application a where a.job.recruiter.id = :rid")
    long countForRecruiter(@Param("rid") Long recruiterId);

    @Query("select a.status, count(a) from Application a where a.job.recruiter.id = :rid group by a.status")
    List<Object[]> countByStatusForRecruiter(@Param("rid") Long recruiterId);

    @Query("select avg(a.matchScore) from Application a where a.job.recruiter.id = :rid and a.matchScore is not null")
    Double averageScoreForRecruiter(@Param("rid") Long recruiterId);

    @Query("""
            select j.id, j.title, count(a), avg(a.matchScore)
            from Job j left join Application a on a.job = j
            where j.recruiter.id = :rid
            group by j.id, j.title
            order by count(a) desc, j.id desc
            """)
    List<Object[]> perJobForRecruiter(@Param("rid") Long recruiterId);
}
