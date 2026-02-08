package com.jobflow.api.repository;

import com.jobflow.api.model.Job;
import com.jobflow.api.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(JobStatus status);
    List<Job> findAllByOrderByCreatedAtDesc();
}