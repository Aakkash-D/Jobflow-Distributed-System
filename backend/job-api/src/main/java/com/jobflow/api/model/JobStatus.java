package com.jobflow.api.model;

public enum JobStatus {
    CREATED,    // Job created but not queued yet
    QUEUED,     // Job pushed to Redis queue
    RUNNING,    // Worker is processing
    COMPLETED,  // Successfully finished
    FAILED      // Permanently failed (exhausted retries)
}