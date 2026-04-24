package com.uet.ticketrush.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest (
        @NotBlank(message = "Mật khẩu cũ không được để trống")
         String oldPassword,
        @NotBlank
        @Size(min = 8, max = 32, message = "Mật khẩu mới phải từ 8 đến 32 ký tự")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "Mật khẩu mới phải chứa chữ hoa, chữ thường và số"
        )
         String newPassword
) {
}
