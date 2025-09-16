package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.email.to}")
    private String toEmail;

    @Value("${notification.email.subject.prefix}")
    private String subjectPrefix;

    public void sendCircuitBreakerNotification(String serviceName, String state, String details) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(String.format("%s %s is now %s", subjectPrefix, serviceName, state));
            message.setText(String.format(
                "Circuit Breaker Notification\n\n" +
                "Service: %s\n" +
                "New State: %s\n" +
                "Time: %s\n\n" +
                "Details: %s",
                serviceName,
                state,
                java.time.LocalDateTime.now(),
                details
            ));

            mailSender.send(message);
            logger.info("Circuit breaker notification email sent for service: {}, state: {}", serviceName, state);
        } catch (Exception e) {
            logger.error("Failed to send circuit breaker notification email", e);
        }
    }
}
