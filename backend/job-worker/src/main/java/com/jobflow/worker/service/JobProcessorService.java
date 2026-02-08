package com.jobflow.worker.service;

import com.jobflow.worker.model.Job;
import com.jobflow.worker.model.JobStatus;
import com.jobflow.worker.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class JobProcessorService {
    
    private static final String JOB_QUEUE = "job:queue";
    private static final String DLQ_QUEUE = "job:dlq";
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * Poll Redis queue every 2 seconds for new jobs
     */
    @Scheduled(fixedDelay = 2000)
    public void pollAndProcessJobs() {
        try {
            // Blocking right pop with 1 second timeout
            String jobIdStr = redisTemplate.opsForList()
                    .rightPop(JOB_QUEUE, 1, TimeUnit.SECONDS);
            
            if (jobIdStr != null) {
                Long jobId = Long.parseLong(jobIdStr);
                processJob(jobId);
            }
        } catch (Exception e) {
            System.err.println("❌ Error polling queue: " + e.getMessage());
        }
    }
    
    /**
     * Process a single job
     */
    private void processJob(Long jobId) {
        Job job = jobRepository.findById(jobId).orElse(null);
        
        if (job == null) {
            System.err.println("❌ Job not found: " + jobId);
            return;
        }
        
        System.out.println("⚙️  Processing job: " + jobId + " (Type: " + job.getJobType() + ")");
        
        // Update status to RUNNING
        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        jobRepository.save(job);
        
        try {
            // SIMULATE JOB EXECUTION
            executeJob(job);
            
            // Mark as COMPLETED
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setErrorMessage(null);
            jobRepository.save(job);
            
            System.out.println("✅ Job completed: " + jobId);
            
        } catch (Exception e) {
            System.err.println("⚠️  Job failed: " + jobId + " - " + e.getMessage());
            handleJobFailure(job, e);
        }
    }
    
    /**
     * Simulate job execution based on job type
     */
    private void executeJob(Job job) throws Exception {
        String jobType = job.getJobType();
        
        switch (jobType) {
            case "email":
                simulateEmailJob(job);
                break;
            case "report":
                simulateReportJob(job);
                break;
            case "data-processing":
                simulateDataProcessingJob(job);
                break;
            case "fail":
                // Intentional failure for testing
                throw new RuntimeException("Simulated failure");
            default:
                simulateDefaultJob(job);
        }
    }
    
    private void simulateEmailJob(Job job) throws InterruptedException {
        System.out.println("📧 Sending email...");
        Thread.sleep(3000); // Simulate 3 second work
        System.out.println("📧 Email sent successfully");
    }
    
    private void simulateReportJob(Job job) throws InterruptedException {
        System.out.println("📊 Generating report...");
        Thread.sleep(5000); // Simulate 5 second work
        System.out.println("📊 Report generated successfully");
    }
    
    private void simulateDataProcessingJob(Job job) throws InterruptedException {
        System.out.println("🔄 Processing data...");
        Thread.sleep(4000); // Simulate 4 second work
        System.out.println("🔄 Data processed successfully");
    }
    
    private void simulateDefaultJob(Job job) throws InterruptedException {
        System.out.println("⚙️  Executing default job...");
        Thread.sleep(2000); // Simulate 2 second work
        System.out.println("⚙️  Default job completed");
    }
    
    /**
     * Handle job failure with retry logic
     */
    private void handleJobFailure(Job job, Exception e) {
        job.setErrorMessage(e.getMessage());
        job.setRetryCount(job.getRetryCount() + 1);
        
        if (job.getRetryCount() < job.getMaxRetries()) {
            // Re-queue for retry
            job.setStatus(JobStatus.QUEUED);
            jobRepository.save(job);
            
            redisTemplate.opsForList().leftPush(JOB_QUEUE, job.getId().toString());
            
            System.out.println("🔄 Job re-queued for retry: " + job.getId() + 
                             " (Attempt " + (job.getRetryCount() + 1) + "/" + job.getMaxRetries() + ")");
        } else {
            // Move to Dead Letter Queue
            job.setStatus(JobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
            
            redisTemplate.opsForList().leftPush(DLQ_QUEUE, job.getId().toString());
            
            System.err.println("💀 Job moved to DLQ (exhausted retries): " + job.getId());
        }
    }
}