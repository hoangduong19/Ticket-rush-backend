package com.uet.ticketrush.dtos;

import com.uet.ticketrush.enums.Gender;
import com.uet.ticketrush.models.User;
import lombok.Builder;

@Builder
public record UserInformationResponseDTO (
        String displayName,
        Integer age,
        Gender gender,
        String username,
        String avatarUrl
) {
    public static UserInformationResponseDTO fromEntity(User user) {
        return new UserInformationResponseDTO(
                user.getDisplayName(),
                user.getAge(),
                user.getGender(),
                user.getUsername(),
                user.getAvatarUrl()
        );
    }
}
