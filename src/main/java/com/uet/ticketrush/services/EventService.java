package com.uet.ticketrush.services;

import com.uet.ticketrush.dtos.EventRequestDTO;
import com.uet.ticketrush.enums.EventStatus;
import com.uet.ticketrush.models.Event;
import com.uet.ticketrush.repos.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

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
