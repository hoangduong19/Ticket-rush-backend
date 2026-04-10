package com.uet.ticketrush.controllers;

import com.uet.ticketrush.dtos.EventRequestDTO;
import com.uet.ticketrush.models.Event;
import com.uet.ticketrush.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class EventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody EventRequestDTO request) {
        Event newEvent = eventService.createEvent(request);

        return new ResponseEntity<>(newEvent, HttpStatus.CREATED);
    }

}
