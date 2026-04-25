package com.uet.ticketrush.services;

import com.uet.ticketrush.models.Admin;
import com.uet.ticketrush.models.AdminPrincipal;
import com.uet.ticketrush.repos.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {
    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username);

        if (admin == null) {
            throw new UsernameNotFoundException(username);
        }

        return new AdminPrincipal(admin);
    }
}