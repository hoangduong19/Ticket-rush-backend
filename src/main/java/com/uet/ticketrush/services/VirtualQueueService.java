package com.uet.ticketrush.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VirtualQueueService {

    private final StringRedisTemplate redisTemplate;
    // Set to 50 concurrent users per requirement
    private static final int MAX_CONCURRENT_USERS = 50;

    public Map<String, Object> joinQueue(UUID eventId, String userId) {
        String activeKey = "event:" + eventId + ":active";
        String queueKey  = "event:" + eventId + ":queue";

        // Check if user is already active
        Double activeScore = redisTemplate.opsForZSet().score(activeKey, userId);
        if (activeScore != null) {
            redisTemplate.opsForZSet().add(activeKey, userId, Instant.now().toEpochMilli()); // refresh active timestamp
            return Map.of("status", "ACTIVE");
        }

        // Check if user is already in queue
        Long rank = redisTemplate.opsForZSet().rank(queueKey, userId);
        if (rank != null) {
            return Map.of("status", "WAITING", "position", rank + 1);
        }

        Long activeCount = redisTemplate.opsForZSet().zCard(activeKey);
        if (activeCount != null && activeCount < MAX_CONCURRENT_USERS) {
            Long queueSize = redisTemplate.opsForZSet().zCard(queueKey);
            if (queueSize == null || queueSize == 0) { // If no queue, go straight to active
                redisTemplate.opsForZSet().add(activeKey, userId, Instant.now().toEpochMilli());
                return Map.of("status", "ACTIVE");
            }
        }

        // Add to queue
        redisTemplate.opsForZSet().add(queueKey, userId, Instant.now().toEpochMilli());
        rank = redisTemplate.opsForZSet().rank(queueKey, userId);
        return Map.of("status", "WAITING", "position", (rank != null ? rank + 1 : 1));
    }

    public Map<String, Object> getQueueStatus(UUID eventId, String userId) {
        String activeKey = "event:" + eventId + ":active";
        String queueKey  = "event:" + eventId + ":queue";

        // Refresh if active
        Double activeScore = redisTemplate.opsForZSet().score(activeKey, userId);
        if (activeScore != null) {
            redisTemplate.opsForZSet().add(activeKey, userId, Instant.now().toEpochMilli());
            return Map.of("status", "ACTIVE");
        }

        // Process queue to check if we can promote users to active
        processQueue(eventId);

        // Check if we became active after processQueue
        activeScore = redisTemplate.opsForZSet().score(activeKey, userId);
        if (activeScore != null) {
            return Map.of("status", "ACTIVE");
        }

        Long rank = redisTemplate.opsForZSet().rank(queueKey, userId);
        if (rank != null) {
            return Map.of("status", "WAITING", "position", rank + 1);
        }

        return Map.of("status", "NOT_IN_QUEUE");
    }

    public void releaseSlot(UUID eventId, String userId) {
        String activeKey = "event:" + eventId + ":active";
        redisTemplate.opsForZSet().remove(activeKey, userId);
        
        // Immediately try to promote users from queue
        processQueue(eventId);
    }

    private void processQueue(UUID eventId) {
        String activeKey = "event:" + eventId + ":active";
        String queueKey  = "event:" + eventId + ":queue";

        Long activeCount = redisTemplate.opsForZSet().zCard(activeKey);
        Long queueSize = redisTemplate.opsForZSet().zCard(queueKey);

        if (activeCount != null && activeCount < MAX_CONCURRENT_USERS && queueSize != null && queueSize > 0) {
            long availableSlots = MAX_CONCURRENT_USERS - activeCount;
            Set<String> nextUsers = redisTemplate.opsForZSet().range(queueKey, 0, availableSlots - 1);
            if (nextUsers != null && !nextUsers.isEmpty()) {
                for (String u : nextUsers) {
                    redisTemplate.opsForZSet().add(activeKey, u, Instant.now().toEpochMilli());
                    redisTemplate.opsForZSet().remove(queueKey, u);
                }
            }
        }
    }
}
