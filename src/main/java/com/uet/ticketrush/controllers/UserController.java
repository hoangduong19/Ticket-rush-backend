package com.uet.ticketrush.controllers;

import com.uet.ticketrush.dtos.UserInformationResponseDTO;
import com.uet.ticketrush.dtos.UserUpdateProfileDTO;
import com.uet.ticketrush.models.User;
import com.uet.ticketrush.services.MyUserDetailsService;
import com.uet.ticketrush.services.UserService;
import com.uet.ticketrush.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

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
        String currentUsername = SecurityUtils.getCurrentUsername(); //hardcoded
        System.out.println(currentUsername);
        User updatedUser = userService.updateProfile(currentUsername, dto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserInformationResponseDTO> getMyProfile() {
        String currentUsername = SecurityUtils.getCurrentUsername(); //hardcoded
        UserInformationResponseDTO dto = userService.getDataByUsername(currentUsername);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/users/me/avatar")
    public ResponseEntity<?> updateAvatar(@RequestParam("file")MultipartFile file) {
        String currentUsername = SecurityUtils.getCurrentUsername(); //hardcode
        String newUrl = userService.updateProfileAvatar(currentUsername, file);
        return ResponseEntity.ok(Map.of("url", newUrl));
    }
}