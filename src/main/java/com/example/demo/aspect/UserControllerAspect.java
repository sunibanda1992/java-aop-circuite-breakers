package com.example.demo.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.util.LoggingUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect for logging API calls using the @LogApiCall annotation.
 */
@Aspect
@Component
public class UserControllerAspect {

    private final Logger log = LoggerFactory.getLogger("UserControllerLogger");

    /**
     * Pointcut that matches all methods annotated with @LogApiCall
     */
    @Pointcut("@annotation(com.example.demo.aspect.LogApiCall)")
    public void logApiCallMethods() {
        // Method is empty as this is just a Pointcut
    }
    
    /**
     * Pointcut that matches all classes annotated with @LogApiCall
     */
    @Pointcut("@within(com.example.demo.aspect.LogApiCall)")
    public void logApiCallClasses() {
        // Method is empty as this is just a Pointcut
    }
    
    /**
     * Combined pointcut for both method and class level annotations
     */
    @Pointcut("logApiCallMethods() || logApiCallClasses()")
    public void logApiCall() {
        // Method is empty as this is just a Pointcut
    }

    /**
     * Log method parameters before method execution
     */
    @Before("logApiCall()")
    public void logMethodParams(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Check if we should log parameters
        LogApiCall logApiCall = getLogApiCallAnnotation(method, joinPoint);
        if (logApiCall != null && !logApiCall.logParams()) {
            return;
        }
        
        // Extract method description
        String description = "";
        if (logApiCall != null && !logApiCall.value().isEmpty()) {
            description = " - " + logApiCall.value();
        }
        
        // Extract method parameters
        Map<String, Object> parameters = extractMethodParameters(joinPoint, method);
        
        // Mask sensitive data
        Map<String, Object> maskedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            maskedParams.put(entry.getKey(), LoggingUtils.maskSensitiveData(entry.getValue()));
        }
        
        log.info("⬇️ [REST API CALL] {}.{}(){}  with parameters: {}", 
                joinPoint.getSignature().getDeclaringType().getSimpleName(), 
                method.getName(),
                description,
                maskedParams);
    }

    /**
     * Log method return values after method execution
     */
    @AfterReturning(pointcut = "logApiCall()", returning = "result")
    public void logMethodReturn(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Check if we should log response
        LogApiCall logApiCall = getLogApiCallAnnotation(method, joinPoint);
        if (logApiCall != null && !logApiCall.logResponse()) {
            return;
        }
        
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
            log.info("⬆️ [REST API RESPONSE] {}.{}() returned: status={}, body={}", 
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(), 
                    responseEntity.getStatusCode(),
                    LoggingUtils.maskSensitiveData(responseEntity.getBody()));
        } else {
            log.info("⬆️ [REST API RESPONSE] {}.{}() returned: {}", 
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(), 
                    LoggingUtils.maskSensitiveData(result));
        }
    }
    
    /**
     * Log method execution time
     */
    @Around("logApiCall()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Check if we should log execution time
        LogApiCall logApiCall = getLogApiCallAnnotation(method, joinPoint);
        if (logApiCall != null && !logApiCall.logExecutionTime()) {
            return joinPoint.proceed();
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.info("⏱️ [REST API TIMING] {}.{}() executed in {} ms", 
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(), 
                    duration);
            
            return result;
        } catch (Exception e) {
            log.error("❌ [REST API ERROR] {}.{}() threw exception: {}", 
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(), 
                    e.getMessage());
            throw e;
        }
    }
    
    /**
     * Helper method to extract method parameters with annotations
     */
    private Map<String, Object> extractMethodParameters(JoinPoint joinPoint, Method method) {
        Map<String, Object> parameters = new HashMap<>();
        
        Parameter[] methodParameters = method.getParameters();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();
        
        for (int i = 0; i < methodParameters.length; i++) {
            if (i < args.length) {
                String paramName = methodParameters[i].getName();
                Object paramValue = args[i];
                
                // Look for specific annotations to get better parameter names
                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation instanceof PathVariable) {
                        PathVariable pathVariable = (PathVariable) annotation;
                        if (!pathVariable.value().isEmpty()) {
                            paramName = "PathVariable:" + pathVariable.value();
                        } else {
                            paramName = "PathVariable:" + paramName;
                        }
                    } else if (annotation instanceof RequestBody) {
                        paramName = "RequestBody:" + paramName;
                    }
                }
                
                parameters.put(paramName, paramValue);
            }
        }
        
        return parameters;
    }
    
    /**
     * Helper method to get the LogApiCall annotation from either the method or the class
     */
    private LogApiCall getLogApiCallAnnotation(Method method, JoinPoint joinPoint) {
        // Check method level annotation first
        LogApiCall annotation = method.getAnnotation(LogApiCall.class);
        if (annotation != null) {
            return annotation;
        }
        
        // If not found, check class level annotation
        Class<?> targetClass = joinPoint.getTarget().getClass();
        return targetClass.getAnnotation(LogApiCall.class);
    }
}
