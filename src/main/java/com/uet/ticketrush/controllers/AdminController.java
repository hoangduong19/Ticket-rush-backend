package com.uet.ticketrush.controllers;

import com.uet.ticketrush.models.Admin;
import com.uet.ticketrush.models.User;
import com.uet.ticketrush.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping("/admins")
    public List<Admin> getAdmin() {
        return adminService.findAll();
    }

    @PostMapping("/adminLogin")
    public String login(@RequestBody Admin admin) {
        return adminService.verify(admin);
    }
}