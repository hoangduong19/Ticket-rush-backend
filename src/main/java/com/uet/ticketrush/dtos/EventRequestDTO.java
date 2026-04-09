package com.uet.ticketrush.dtos;

import com.uet.ticketrush.enums.EventCategory;

import java.time.LocalDateTime;

public record EventRequestDTO(
        String title,
        String description,
        String location,
        LocalDateTime date,
        String bannerUrl,
        EventCategory category
) {}
