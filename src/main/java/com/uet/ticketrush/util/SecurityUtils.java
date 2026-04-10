package com.uet.ticketrush.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    /**
     * Lấy Username của người dùng hiện tại đang đăng nhập
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else {
                return principal.toString(); // Thường là username kiểu String
            }
        }
        return null;
    }

    /**
     * Lấy toàn bộ đối tượng UserDetails (nếu bạn cần nhiều thông tin hơn email, role...)
     */
    public static UserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        }
        return null;
    }
}