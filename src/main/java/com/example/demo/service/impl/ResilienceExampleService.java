package com.example.demo.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * Example service demonstrating Resilience4j features.
 * This is a sample implementation that shows how to use various Resilience4j
 * patterns in your services.
 */
@Service
public class ResilienceExampleService {

    private static final Logger logger = LoggerFactory.getLogger(ResilienceExampleService.class);
    private static final String USER_SERVICE = "userService";

    /**
     * Example of a method protected by a circuit breaker.
     * If this method fails frequently enough, the circuit will open
     * and prevent further calls for a period of time.
     */
    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "fallbackForExternalService")
    public String callExternalService(String input) {
        logger.info("Calling potentially unstable external service with: {}", input);
        
        // Simulate an external service call that might fail
        if (Math.random() < 0.3) {
            logger.warn("External service call failed");
            throw new RuntimeException("External service failed");
        }
        
        return "External service response for: " + input;
    }

    /**
     * Fallback method for the circuit breaker.
     * This will be called when the circuit is open.
     */
    public String fallbackForExternalService(String input, Exception ex) {
        logger.warn("Fallback method called for input: {} with exception: {}", input, ex.getMessage());
        return "Fallback response for: " + input;
    }

    /**
     * Example of a method protected by rate limiting.
     * This will limit the number of calls to this method based on the configuration.
     */
    @RateLimiter(name = USER_SERVICE)
    public String rateLimitedOperation(String input) {
        logger.info("Executing rate limited operation with: {}", input);
        return "Rate limited operation response for: " + input;
    }

    /**
     * Example of a method with automatic retries.
     * If this method fails, it will be retried based on the configuration.
     */
    @Retry(name = USER_SERVICE)
    public String retryableOperation(String input) {
        logger.info("Executing retryable operation with: {}", input);
        
        // Simulate a failure that should be retried
        if (Math.random() < 0.5) {
            logger.warn("Retryable operation failed, will retry");
            throw new RuntimeException("Temporary failure in retryable operation");
        }
        
        return "Retryable operation response for: " + input;
    }
    
    /**
     * Example of combining multiple resilience patterns.
     * This method is protected by a circuit breaker, rate limiter, and retry mechanism.
     */
    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "fallbackForCombinedOperation")
    @RateLimiter(name = USER_SERVICE)
    @Retry(name = USER_SERVICE)
    public String combinedResilienceOperation(String input) {
        logger.info("Executing operation with combined resilience patterns for: {}", input);
        
        // Simulate a failure that should be retried and might trigger circuit breaker
        if (Math.random() < 0.4) {
            logger.warn("Combined operation failed");
            throw new RuntimeException("Failure in combined operation");
        }
        
        return "Combined resilience operation response for: " + input;
    }
    
    /**
     * Fallback for the combined operation.
     */
    public String fallbackForCombinedOperation(String input, Exception ex) {
        logger.warn("Fallback for combined operation called for input: {} with exception: {}", 
                input, ex.getMessage());
        return "Fallback for combined operation: " + input;
    }
}
