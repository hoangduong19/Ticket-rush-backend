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

    // Mỗi user/IP có 1 bucket riêng
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(2, Refill.intervally(1, Duration.ofSeconds(5))))
                .build();
                // ban dau co 2 lan update, moi 5s hoi 1 token de update
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // Chỉ chặn API update profile
        if (!(request.getRequestURI().contains("/users/me") && request.getMethod().equalsIgnoreCase("PUT"))) {
            chain.doFilter(request, response);
            return;
        }

        // Key = userId nếu đã đăng nhập, fallback về IP, neu ko co ip thi = unknown_client
        String key = SecurityUtils.getCurrentUsername();
        if (key == null || key.trim().isEmpty() || "anonymousUser".equals(key)) {
            key = request.getHeader("X-Forwarded-For");
            if (key == null || key.isEmpty()) {
                key = request.getRemoteAddr();
            }
            if (key == null) {
                key = "unknown_client";
            }
        }

        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                  "error": "Too Many Requests",
                  "message": "You are making requests too quickly. Please wait a moment and try again."
                }
            """);
        }
    }
}