package com.uet.ticketrush.dtos;

import com.uet.ticketrush.enums.EventCategory;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EventFilterDTO(
        List<EventCategory> category,
        LocalDate dateFrom,
        LocalDate dateTo,
        BigDecimal priceMin,
        BigDecimal priceMax,

        @PositiveOrZero
        int page,

        @Positive
        int size
) {
    // Default values
    public EventFilterDTO {
        if (page < 0) page = 0;
        if (size <= 0) size = 9;
    }
}