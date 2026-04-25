package com.uet.ticketrush.services;

import com.uet.ticketrush.models.Admin;
import com.uet.ticketrush.repos.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    @Qualifier("adminAuthenticationManager")
    private AuthenticationManager authManager;

    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    public String verify(Admin admin) {
        Authentication authentication =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                admin.getUsername(),
                                admin.getPassword()
                        ));

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(admin.getUsername());
        }

        return "fail";
    }
}