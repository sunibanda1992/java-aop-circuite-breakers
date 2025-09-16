package com.example.demo.config;

import com.example.demo.service.EmailService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CircuitBreakerConfig {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerConfig.class);

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private EmailService emailService;

    @jakarta.annotation.PostConstruct
    public void configureCircuitBreakerListeners() {
        CircuitBreaker userServiceCircuitBreaker = circuitBreakerRegistry.circuitBreaker("userService");
        
        // Add listeners for all circuit breaker events
        userServiceCircuitBreaker.getEventPublisher()
            .onCallNotPermitted(event -> {
                logger.debug("Call not permitted for circuit breaker '{}' - Request rejected", 
                    event.getCircuitBreakerName());
            })
            .onError(event -> {
                logger.debug("Error in circuit breaker '{}': {} after {}ms",
                    event.getCircuitBreakerName(),
                    event.getThrowable().toString(),
                    event.getElapsedDuration().toMillis());
            })
            .onFailureRateExceeded(event -> {
                logger.debug("Failure rate exceeded in circuit breaker '{}': {}",
                    event.getCircuitBreakerName(),
                    event.getFailureRate());
            })
            .onSlowCallRateExceeded(event -> {
                logger.debug("Slow call rate exceeded in circuit breaker '{}': {}",
                    event.getCircuitBreakerName(),
                    event.getSlowCallRate());
            });
        
        userServiceCircuitBreaker.getEventPublisher()
            .onStateTransition(event -> {
                CircuitBreaker.State newState = event.getStateTransition().getToState();
                String serviceName = event.getCircuitBreakerName();

                logger.info("Circuit breaker '{}' transitioned to state: {}", serviceName, newState);

                switch (newState) {
                    case OPEN:
                        emailService.sendCircuitBreakerNotification(
                            serviceName, 
                            "OPEN", 
                            "Circuit breaker has transitioned to OPEN state. Service is failing and no requests will be allowed."
                        );
                        break;
                    case HALF_OPEN:
                        emailService.sendCircuitBreakerNotification(
                            serviceName, 
                            "HALF_OPEN", 
                            "Circuit breaker has transitioned to HALF_OPEN state. Limited requests will be allowed to test if service is recovered."
                        );
                        break;
                    case CLOSED:
                        emailService.sendCircuitBreakerNotification(
                            serviceName, 
                            "CLOSED", 
                            "Circuit breaker has transitioned to CLOSED state. Service has recovered and is operating normally."
                        );
                        break;
                    default:
                        // Do nothing for other states
                }
            });
    }
}
