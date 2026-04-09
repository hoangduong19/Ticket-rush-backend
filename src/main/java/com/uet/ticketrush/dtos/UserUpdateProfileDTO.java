package com.uet.ticketrush.dtos;

import com.uet.ticketrush.enums.Gender;

public record UserUpdateProfileDTO (
        String displayName,
        Integer age,
        Gender gender
) {}
