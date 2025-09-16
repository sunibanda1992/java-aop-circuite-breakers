package com.example.demo.util;

import com.example.demo.aspect.LogSensitive;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for masking sensitive data in logs.
 */
public class LoggingUtils {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingUtils.class);
    
    /**
     * Masks sensitive fields in an object for logging purposes.
     * 
     * @param obj The object to mask sensitive fields in.
     * @return A string representation with masked sensitive fields.
     */
    public static String maskSensitiveData(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        // Handle simple types
        if (obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        
        // Handle collections
        if (obj instanceof Collection) {
            return handleCollection((Collection<?>) obj);
        }
        
        // Handle maps
        if (obj instanceof Map) {
            return handleMap((Map<?, ?>) obj);
        }
        
        // Handle complex objects
        StringBuilder sb = new StringBuilder();
        sb.append(obj.getClass().getSimpleName()).append("{");
        
        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            boolean first = true;
            
            for (Field field : fields) {
                field.setAccessible(true);
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                
                sb.append(field.getName()).append("=");
                
                // Check if field has @LogSensitive annotation
                LogSensitive logSensitive = field.getAnnotation(LogSensitive.class);
                if (logSensitive != null && field.get(obj) != null) {
                    sb.append(maskValue(field.get(obj).toString(), logSensitive));
                } else {
                    Object value = field.get(obj);
                    if (value != null && value.getClass().getPackage() != null && 
                            value.getClass().getPackage().getName().startsWith("com.example.demo")) {
                        // Recursively handle nested objects from our application
                        sb.append(maskSensitiveData(value));
                    } else {
                        sb.append(value);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error masking sensitive data: {}", e.getMessage());
            return obj.toString() + " (Error masking sensitive data)";
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Masks a string value according to the LogSensitive annotation.
     */
    private static String maskValue(String value, LogSensitive annotation) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        
        int showFirst = annotation.showFirst();
        int showLast = annotation.showLast();
        char maskChar = annotation.maskChar();
        
        // Adjust if the value is shorter than what we want to show
        int valueLength = value.length();
        if (showFirst + showLast >= valueLength) {
            showFirst = Math.min(showFirst, valueLength);
            showLast = Math.min(showLast, valueLength - showFirst);
        }
        
        StringBuilder maskedValue = new StringBuilder();
        
        // Add visible characters at the beginning
        if (showFirst > 0) {
            maskedValue.append(value, 0, showFirst);
        }
        
        // Add mask characters
        int maskLength = valueLength - showFirst - showLast;
        for (int i = 0; i < maskLength; i++) {
            maskedValue.append(maskChar);
        }
        
        // Add visible characters at the end
        if (showLast > 0) {
            maskedValue.append(value.substring(valueLength - showLast));
        }
        
        return maskedValue.toString();
    }
    
    /**
     * Handles masking of sensitive data in collections.
     */
    private static String handleCollection(Collection<?> collection) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        
        for (Object item : collection) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(maskSensitiveData(item));
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Handles masking of sensitive data in maps.
     */
    private static String handleMap(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(entry.getKey()).append("=").append(maskSensitiveData(entry.getValue()));
        }
        
        sb.append("}");
        return sb.toString();
    }
}
