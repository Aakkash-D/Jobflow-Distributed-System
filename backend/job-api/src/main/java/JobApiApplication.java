package com.jobflow.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JobApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobApiApplication.class, args);
        System.out.println("🚀 Job API started on http://localhost:8080");
    }
}