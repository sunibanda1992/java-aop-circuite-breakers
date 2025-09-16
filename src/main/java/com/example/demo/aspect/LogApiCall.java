package com.example.demo.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be logged by the aspect.
 * This allows more targeted and controlled logging.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogApiCall {
    
    /**
     * Optional description for the API call
     */
    String value() default "";
    
    /**
     * Whether to log request parameters
     */
    boolean logParams() default true;
    
    /**
     * Whether to log response
     */
    boolean logResponse() default true;
    
    /**
     * Whether to log execution time
     */
    boolean logExecutionTime() default true;
}
