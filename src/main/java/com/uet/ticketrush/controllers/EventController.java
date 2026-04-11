package com.uet.ticketrush.controllers;

import com.uet.ticketrush.dtos.EventRequestDTO;
import com.uet.ticketrush.dtos.SeatingPayloadDTO;
import com.uet.ticketrush.models.Event;
import com.uet.ticketrush.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping
@CrossOrigin(origins = "http://localhost:3000")
public class EventController {
    private final EventService eventService;

    @PostMapping("/admin/events")
    public ResponseEntity<Event> createEvent(@RequestBody EventRequestDTO request) {
        Event newEvent = eventService.createEvent(request);

        return new ResponseEntity<>(newEvent, HttpStatus.CREATED);
    }

    // THÊM HÀM NÀY ĐỂ TRẢ VỀ DANH SÁCH SỰ KIỆN (Nhiệm vụ 1)
    @GetMapping("/events")
    public ResponseEntity<?> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<Event> getEventById(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    @PostMapping("/events/{eventId}/seats")
    public ResponseEntity<String> setupSeats(@PathVariable UUID eventId, @RequestBody SeatingPayloadDTO payload) {
        eventService.generateSeatsFromConfig(eventId, payload);
        return ResponseEntity.ok("Đã tạo ma trận ghế thành công!");
    }
}
