package com.uet.ticketrush.dtos;

public record ErrorResponseDTO(
        int status,
        String message,
        long timestamp
) {}