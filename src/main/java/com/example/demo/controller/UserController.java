package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.aspect.LogApiCall;
import com.example.demo.dto.UserDto;
import com.example.demo.service.UserService;

/**
 * REST controller for managing User operations.
 * Logging is handled by UserControllerAspect.
 */
@RestController
@RequestMapping("/api/users")
@LogApiCall
public class UserController {

    @Autowired
    private UserService userService;

    // Create user
    @PostMapping
    @LogApiCall(value = "Create new user", logResponse = false)
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {

        UserDto createdUser = userService.createUser(userDto);

        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // Get user by ID
    @GetMapping("/{id}")
    @LogApiCall("Get user by ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {

        UserDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }

    // Get all users
    @GetMapping
    @LogApiCall(value = "Get all users", logResponse = false)
    public ResponseEntity<List<UserDto>> getAllUsers() {

        List<UserDto> users = userService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    // Update user
    @PutMapping("/{id}")
    @LogApiCall("Update existing user")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
            @RequestBody UserDto userDto) {

        UserDto updatedUser = userService.updateUser(id, userDto);

        return ResponseEntity.ok(updatedUser);
    }

    // Delete user
    @DeleteMapping("/{id}")
    @LogApiCall("Delete user")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok("User deleted successfully");
    }
}
