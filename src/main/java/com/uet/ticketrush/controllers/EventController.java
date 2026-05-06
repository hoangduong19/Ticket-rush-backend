package com.uet.ticketrush.controllers;

import com.uet.ticketrush.dtos.EventRequestDTO;
import com.uet.ticketrush.dtos.SeatingPayloadDTO;
import com.uet.ticketrush.models.Event;
import com.uet.ticketrush.models.Seat;
import com.uet.ticketrush.services.EventService;
import com.uet.ticketrush.services.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping
@CrossOrigin(origins = "http://localhost:3000")
public class EventController {
    private final EventService eventService;

    private final SeatService seatService;

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

    @GetMapping("/events/{eventId}/seats")
    public ResponseEntity<?> getSeatsByEventId(@PathVariable UUID eventId) {
        return ResponseEntity.ok(seatService.getSeatsByEventId(eventId));
    }

    @PostMapping("/events/{eventId}/seats")
    public ResponseEntity<String> setupSeats(@PathVariable UUID eventId, @RequestBody SeatingPayloadDTO payload) {
        eventService.generateSeatsFromConfig(eventId, payload);
        return ResponseEntity.ok("Đã tạo ma trận ghế thành công!");
    }

    @GetMapping("/events/{eventId}/seats/status")
    public ResponseEntity<List<Seat>> getSeatsStatus(@PathVariable UUID eventId) {
        List<Seat> seats = eventService.getSeatsStatus(eventId);
        return ResponseEntity.ok(seats);
    }

    @PostMapping(value = "/admin/events", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Event> createEvent(
            @RequestPart("event") EventRequestDTO request, // Nhận thông tin chữ
            @RequestPart("file") MultipartFile file) {    // Nhận tệp ảnh

        Event newEvent = eventService.createEventWithImage(request, file);
        return new ResponseEntity<>(newEvent, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/events/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable UUID eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok().build();
    }
}
