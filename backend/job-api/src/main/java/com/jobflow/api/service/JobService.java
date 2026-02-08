package com.jobflow.api.service;

import com.jobflow.api.model.Job;
import com.jobflow.api.model.JobStatus;
import com.jobflow.api.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {
    
    private static final String JOB_QUEUE = "job:queue";
    private static final String DLQ_QUEUE = "job:dlq";
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * Create a new job, save to DB, and enqueue to Redis
     */
    public Job createJob(String jobType, String payload) {
        // Save job to database with CREATED status
        Job job = new Job(jobType, payload);
        job = jobRepository.save(job);
        
        // Push job ID to Redis queue
        job.setStatus(JobStatus.QUEUED);
        jobRepository.save(job);
        
        redisTemplate.opsForList().leftPush(JOB_QUEUE, job.getId().toString());
        
        System.out.println("✅ Job created and queued: " + job.getId());
        return job;
    }
    
    /**
     * Get all jobs, ordered by creation time (newest first)
     */
    public List<Job> getAllJobs() {
        return jobRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get job by ID
     */
    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }
    
    /**
     * Get jobs by status
     */
    public List<Job> getJobsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }
    
    /**
     * Get current queue size
     */
    public Long getQueueSize() {
        return redisTemplate.opsForList().size(JOB_QUEUE);
    }
    
    /**
     * Get dead letter queue size
     */
    public Long getDLQSize() {
        return redisTemplate.opsForList().size(DLQ_QUEUE);
    }
}