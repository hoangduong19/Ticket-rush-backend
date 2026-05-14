package com.uet.ticketrush.config;

import com.uet.ticketrush.util.SecurityUtils;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Tách riêng 2 map để quản lý: 1 cho Auth (dựa theo IP), 1 cho Profile (dựa theo UserID)
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> profileBuckets = new ConcurrentHashMap<>();

    // Giới hạn cho Login/Register: 1 request / 1 phút (Chống Brute-force/Bot)
    private Bucket newAuthBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(1, Duration.ofMinutes(1))))
                .build();
    }

    // Giới hạn cho Update Profile: 1 request / 5 giây
    private Bucket newProfileBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(2, Refill.intervally(1, Duration.ofSeconds(5))))
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "unknown_client";
    }

    private boolean isAuthEndpoint(String uri, String method) {
        if (!method.equalsIgnoreCase("POST")) return false;
        return uri.endsWith("/login") || uri.endsWith("/adminLogin") ||
                uri.endsWith("/register") || uri.endsWith("/adminRegister");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 1. Xử lý Rate Limit cho Auth (Login, Register)
        if (isAuthEndpoint(uri, method)) {
            String ip = getClientIP(request);
            Bucket bucket = authBuckets.computeIfAbsent(ip, k -> newAuthBucket());

            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                sendRateLimitError(response, "Too many authentication attempts. Please wait 1 minute.");
            }
            return;
        }

        // 2. Xử lý Rate Limit cho Update Profile
        if (uri.contains("/users/me") && method.equalsIgnoreCase("PUT")) {
            String key = SecurityUtils.getCurrentUsername();
            if (key == null || key.trim().isEmpty() || "anonymousUser".equals(key)) {
                key = getClientIP(request); // Fallback
            }

            Bucket bucket = profileBuckets.computeIfAbsent(key, k -> newProfileBucket());

            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                sendRateLimitError(response, "You are updating profile too quickly. Please wait 5 seconds.");
            }
            return;
        }

        // Các request khác cho qua bình thường
        chain.doFilter(request, response);
    }

    private void sendRateLimitError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(String.format("""
            {
              "error": "Too Many Requests",
              "message": "%s"
            }
        """, message));
    }
}