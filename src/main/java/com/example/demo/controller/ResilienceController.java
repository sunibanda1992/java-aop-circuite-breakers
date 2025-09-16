package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.aspect.LogApiCall;
import com.example.demo.service.impl.ResilienceExampleService;

/**
 * Controller demonstrating Resilience4j functionality.
 */
@RestController
@RequestMapping("/api/resilience")
@LogApiCall
public class ResilienceController {

    @Autowired
    private ResilienceExampleService resilienceService;
    
    /**
     * Endpoint demonstrating circuit breaker pattern.
     */
    @GetMapping("/circuit-breaker/{input}")
    @LogApiCall("Circuit Breaker Example")
    public ResponseEntity<String> circuitBreakerExample(@PathVariable String input) {
        String result = resilienceService.callExternalService(input);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Endpoint demonstrating rate limiter pattern.
     */
    @GetMapping("/rate-limiter/{input}")
    @LogApiCall("Rate Limiter Example")
    public ResponseEntity<String> rateLimiterExample(@PathVariable String input) {
        String result = resilienceService.rateLimitedOperation(input);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Endpoint demonstrating retry pattern.
     */
    @GetMapping("/retry/{input}")
    @LogApiCall("Retry Example")
    public ResponseEntity<String> retryExample(@PathVariable String input) {
        String result = resilienceService.retryableOperation(input);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Endpoint demonstrating combined resilience patterns.
     */
    @GetMapping("/combined/{input}")
    @LogApiCall("Combined Resilience Patterns Example")
    public ResponseEntity<String> combinedExample(@PathVariable String input) {
        String result = resilienceService.combinedResilienceOperation(input);
        return ResponseEntity.ok(result);
    }
}
