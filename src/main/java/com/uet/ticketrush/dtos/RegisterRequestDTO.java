package com.uet.ticketrush.dtos;

import com.uet.ticketrush.enums.Gender;
import jakarta.validation.constraints.*;

public record RegisterRequestDTO(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String username,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "Mật khẩu mới phải chứa chữ hoa, chữ thường và số"
        )
        String password,

        @Min(value = 0, message = "Tuổi không hợp lệ")
        @Max(value = 120, message = "Tuổi không hợp lệ")
        Integer age,

        @Size(min = 2, message = "Tên hiển thị phải có ít nhất 2 ký tự")
        String displayName,

        Gender gender
) {

}