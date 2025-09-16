package com.example.demo.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.UserDto;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import com.example.demo.exception.ResourceNotFoundException;

@Service
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        logger.debug("Creating a new user with email: {}", userDto.getEmail());
        
        // Convert DTO to entity
        User user = mapToEntity(userDto);
        
        // Save entity
        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getId());
        
        // Convert entity to DTO
        return mapToDto(savedUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        logger.debug("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
        logger.debug("User found: {}", user.getEmail());
        return mapToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        logger.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        logger.info("Retrieved {} users from database", users.size());
        return users.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        logger.debug("Updating user with ID: {}", id);
        
        // Check if user exists
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Cannot update. User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
        
        // Update user properties
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setAge(userDto.getAge());
        
        // Save updated user
        User updatedUser = userRepository.save(user);
        logger.info("User with ID: {} updated successfully", updatedUser.getId());
        
        return mapToDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        logger.debug("Deleting user with ID: {}", id);
        
        // Check if user exists
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Cannot delete. User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
        
        userRepository.delete(user);
        logger.info("User with ID: {} deleted successfully", id);
    }
    
    // Utility methods for mapping
    private UserDto mapToDto(User user) {
        logger.trace("Converting User entity to UserDto: {}", user.getId());
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setAge(user.getAge());
        // Don't map password to DTO for security reasons
        return userDto;
    }
    
    private User mapToEntity(UserDto userDto) {
        logger.trace("Converting UserDto to User entity");
        User user = new User();
        user.setId(userDto.getId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setAge(userDto.getAge());
        
        // Only set password if it's provided in the DTO
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            // In a real app, you would hash the password here
            user.setPassword(userDto.getPassword());
            logger.debug("Password set for user");
        }
        
        return user;
    }
}
