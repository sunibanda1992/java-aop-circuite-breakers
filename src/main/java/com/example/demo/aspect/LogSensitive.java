package com.example.demo.aspect;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark fields that should be masked in logs for privacy concerns.
 * Use this on sensitive fields like passwords, credit card numbers, etc.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JsonProperty // Make it compatible with Jackson serialization
public @interface LogSensitive {
    
    /**
     * The masking character to use
     */
    char maskChar() default '*';
    
    /**
     * Number of characters to reveal at the beginning (0 means mask all)
     */
    int showFirst() default 0;
    
    /**
     * Number of characters to reveal at the end (0 means mask all)
     */
    int showLast() default 0;
}
