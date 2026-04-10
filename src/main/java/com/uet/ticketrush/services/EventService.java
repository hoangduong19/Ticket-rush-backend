package com.uet.ticketrush.services;

import com.uet.ticketrush.dtos.EventRequestDTO;
import com.uet.ticketrush.enums.EventStatus;
import com.uet.ticketrush.models.Event;
import com.uet.ticketrush.repos.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    // Hàm lấy tất cả sự kiện
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện với ID: " + eventId));
    }

    public Event createEvent(EventRequestDTO dto) {
        Event event = Event.builder()
                .title(dto.title())
                .description(dto.description())
                .location(dto.location())
                .date(dto.date())
                .status(EventStatus.Published)
                .category(dto.category())
                .build();

        event.validateBasicInfo();
        return eventRepository.save(event);
    }

}
