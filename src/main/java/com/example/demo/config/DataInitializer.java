package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing sample data");
        
        // Check if we already have users to avoid duplicates on restart
        long count = userRepository.count();
        if (count == 0) {
            logger.debug("No users found in the database. Creating sample users.");
            
            // Add some sample users
            User user1 = new User(null, "John", "Doe", "john.doe@example.com", 30, "password123");
            User user2 = new User(null, "Jane", "Smith", "jane.smith@example.com", 25, "securepass");
            User user3 = new User(null, "Michael", "Johnson", "michael.johnson@example.com", 35, "mypassword");
            
            userRepository.save(user1);
            logger.debug("Created user: {}", user1.getFirstName());
            
            userRepository.save(user2);
            logger.debug("Created user: {}", user2.getFirstName());
            
            userRepository.save(user3);
            logger.debug("Created user: {}", user3.getFirstName());
            
            logger.info("Sample users have been initialized successfully");
        } else {
            logger.info("Skipping data initialization. Found {} existing users", count);
        }
    }
}
