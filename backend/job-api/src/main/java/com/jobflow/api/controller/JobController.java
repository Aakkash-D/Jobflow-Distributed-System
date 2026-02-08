package com.jobflow.api.controller;

import com.jobflow.api.model.Job;
import com.jobflow.api.model.JobStatus;
import com.jobflow.api.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "http://localhost:5173")
public class JobController {
    
    @Autowired
    private JobService jobService;
    
    /**
     * Create a new job
     * POST /api/jobs
     */
    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody Map<String, String> request) {
        String jobType = request.getOrDefault("jobType", "default");
        String payload = request.getOrDefault("payload", "{}");
        
        Job job = jobService.createJob(jobType, payload);
        return ResponseEntity.ok(job);
    }
    
    /**
     * Get all jobs
     * GET /api/jobs
     */
    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        List<Job> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }
    
    /**
     * Get job by ID
     * GET /api/jobs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return jobService.getJobById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get jobs by status
     * GET /api/jobs/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Job>> getJobsByStatus(@PathVariable JobStatus status) {
        List<Job> jobs = jobService.getJobsByStatus(status);
        return ResponseEntity.ok(jobs);
    }
    
    /**
     * Get system stats
     * GET /api/jobs/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("queueSize", jobService.getQueueSize());
        stats.put("dlqSize", jobService.getDLQSize());
        stats.put("totalJobs", jobService.getAllJobs().size());
        stats.put("completedJobs", jobService.getJobsByStatus(JobStatus.COMPLETED).size());
        stats.put("failedJobs", jobService.getJobsByStatus(JobStatus.FAILED).size());
        stats.put("runningJobs", jobService.getJobsByStatus(JobStatus.RUNNING).size());
        
        return ResponseEntity.ok(stats);
    }
}