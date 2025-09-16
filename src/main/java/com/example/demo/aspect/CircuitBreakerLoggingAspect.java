package com.example.demo.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class CircuitBreakerLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerLoggingAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @AfterThrowing(
        pointcut = "@annotation(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker) || " +
                  "@within(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker)",
        throwing = "exception"
    )
    public void logCircuitBreakerFailure(JoinPoint joinPoint, Exception exception) {
        try {
            // Check if it's a CallNotPermittedException directly
            boolean isCircuitBreakerException = exception instanceof CallNotPermittedException;
            
            // Check if it's a wrapped exception containing circuit breaker info
            if (!isCircuitBreakerException) {
                Throwable cause = exception;
                while (cause != null) {
                    if (cause instanceof CallNotPermittedException) {
                        isCircuitBreakerException = true;
                        break;
                    }
                    cause = cause.getCause();
                }
            }
            
            // Also check circuit breaker state - if any circuit breaker is OPEN
            if (!isCircuitBreakerException) {
                // Get method signature to check for circuit breaker name
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker annotation = 
                    signature.getMethod().getAnnotation(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker.class);
                
                if (annotation != null) {
                    String circuitBreakerName = annotation.name();
                    if (circuitBreakerRegistry.circuitBreaker(circuitBreakerName).getState() == CircuitBreaker.State.OPEN) {
                        isCircuitBreakerException = true;
                    }
                } else {
                    // Check all circuit breakers in case we can't determine the specific one
                    for (CircuitBreaker cb : circuitBreakerRegistry.getAllCircuitBreakers()) {
                        if (cb.getState() == CircuitBreaker.State.OPEN) {
                            isCircuitBreakerException = true;
                            break;
                        }
                    }
                }
            }
            
            // Log circuit breaker related failures
            if (isCircuitBreakerException || exception.getMessage() != null && 
                exception.getMessage().contains("CircuitBreaker")) {
                
                // Get method details
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                String methodName = signature.getMethod().getName();
                String className = signature.getDeclaringType().getSimpleName();
                
                // Get request details if available
                Map<String, Object> requestInfo = new HashMap<>();
                requestInfo.put("method", className + "." + methodName);
                requestInfo.put("arguments", getMethodArguments(joinPoint));
                requestInfo.put("exceptionType", exception.getClass().getName());
                requestInfo.put("exceptionMessage", exception.getMessage());
                
                // Try to get HTTP request details if available
                addHttpRequestDetails(requestInfo);
                
                // Log the failure with request information
                logger.debug("Circuit Breaker - Request failed: {}", 
                    objectMapper.writeValueAsString(requestInfo));
            } else {
                // Still log non-circuit breaker exceptions but with less detail
                logger.debug("Exception in circuit breaker protected method: {} - {}", 
                    exception.getClass().getName(), exception.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error while logging circuit breaker failure", e);
        }
    }

    private Map<String, Object> getMethodArguments(JoinPoint joinPoint) {
        Map<String, Object> args = new HashMap<>();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();
        
        for (int i = 0; i < parameterNames.length; i++) {
            if (i < parameterValues.length) {
                args.put(parameterNames[i], parameterValues[i]);
            }
        }
        
        return args;
    }

    private void addHttpRequestDetails(Map<String, Object> requestInfo) {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // Add request metadata
                requestInfo.put("url", request.getRequestURL().toString());
                requestInfo.put("httpMethod", request.getMethod());
                requestInfo.put("clientIP", request.getRemoteAddr());
                
                // Add request parameters
                Map<String, String> parameters = new HashMap<>();
                Enumeration<String> paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String paramName = paramNames.nextElement();
                    parameters.put(paramName, request.getParameter(paramName));
                }
                requestInfo.put("parameters", parameters);
                
                // Add headers (excluding sensitive information)
                Map<String, String> headers = new HashMap<>();
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    // Skip sensitive headers
                    if (!headerName.toLowerCase().contains("authorization") && 
                        !headerName.toLowerCase().contains("cookie")) {
                        headers.put(headerName, request.getHeader(headerName));
                    }
                }
                requestInfo.put("headers", headers);
            }
        } catch (Exception e) {
            logger.warn("Could not extract HTTP request details", e);
        }
    }
}
