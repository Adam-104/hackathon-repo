package com.smartrecruit.repository;

import com.smartrecruit.model.Job;
import com.smartrecruit.model.JobStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("""
            select j from Job j
            where j.status = :status
              and (lower(j.title) like lower(concat('%', :q, '%'))
                or lower(j.company) like lower(concat('%', :q, '%'))
                or lower(coalesce(j.requiredSkills, '')) like lower(concat('%', :q, '%')))
            order by j.createdAt desc
            """)
    List<Job> search(@Param("status") JobStatus status, @Param("q") String q);

    List<Job> findByRecruiterIdOrderByCreatedAtDesc(Long recruiterId);
}
