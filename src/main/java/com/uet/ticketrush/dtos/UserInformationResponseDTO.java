package com.uet.ticketrush.dtos;

import com.uet.ticketrush.enums.Gender;
import com.uet.ticketrush.models.User;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserInformationResponseDTO (
        UUID userId,
        String displayName,
        Integer age,
        Gender gender,
        String username,
        String avatarUrl
) {
    public static UserInformationResponseDTO fromEntity(User user) {
        return new UserInformationResponseDTO(
                user.getUserId(),
                user.getDisplayName(),
                user.getAge(),
                user.getGender(),
                user.getUsername(),
                user.getAvatarUrl()
        );
    }
}
