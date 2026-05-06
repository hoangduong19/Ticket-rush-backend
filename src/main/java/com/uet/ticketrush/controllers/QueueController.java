package com.uet.ticketrush.controllers;

import com.uet.ticketrush.services.VirtualQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
public class QueueController {

    private final VirtualQueueService queueService;

    public record QueueRequest(UUID eventId, String userId) {}

    @PostMapping("/join")
    public ResponseEntity<Map<String, Object>> joinQueue(@RequestBody QueueRequest request) {
        return ResponseEntity.ok(queueService.joinQueue(request.eventId(), request.userId()));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getQueueStatus(@RequestParam UUID eventId, @RequestParam String userId) {
        Map<String, Object> status = queueService.getQueueStatus(eventId, userId);

        Map<String, Object> response = new HashMap<>(status);

        // 2. Nếu đã ACTIVE, chỉ lấy thêm mốc expiresAt
        if ("ACTIVE".equals(status.get("status"))) {
            LocalDateTime expiry = queueService.validateAndGetSessionExpiry(eventId, UUID.fromString(userId));

            response.put("expiresAt", expiry.atZone(ZoneId.systemDefault()).toInstant().toString());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completeCheckout(@RequestBody QueueRequest request) {
        queueService.releaseSlot(request.eventId(), request.userId());
        return ResponseEntity.ok(Map.of("message", "Checkout completed, slot released"));
    }
}
