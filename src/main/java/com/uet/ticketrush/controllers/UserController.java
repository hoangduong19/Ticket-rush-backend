package com.uet.ticketrush.controllers;

import com.uet.ticketrush.dtos.UserInformationResponseDTO;
import com.uet.ticketrush.dtos.UserUpdateProfileDTO;
import com.uet.ticketrush.models.User;
import com.uet.ticketrush.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public List<User> getUser() {
        return userService.findAll();
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return userService.verify(user);
    }

    @PutMapping("/users/me")
    public ResponseEntity<User> updateProfile(@RequestBody UserUpdateProfileDTO dto) {
        String currentUsername = "testuser@gmail.com"; //hardcoded
        User updatedUser = userService.updateProfile(currentUsername, dto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserInformationResponseDTO> getMyProfile() {
        String currentUsername = "testuser@gmail.com"; //hardcoded
        UserInformationResponseDTO dto = userService.getDataByUsername(currentUsername);
        return ResponseEntity.ok(dto);
    }
}