package com.uet.ticketrush.models;

import com.uet.ticketrush.enums.Gender;
import com.uet.ticketrush.exceptions.TicketRushException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name ="email", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password;

    private Integer age;

    @Column(name = "display_name")
    private String displayName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "avatar_url")
    private String avatarUrl;

    public void updateProfile(String newDisplayName, Integer newAge, Gender gender) {
        if (newDisplayName != null) {
            if (newDisplayName.trim().length() < 2) {
                throw new TicketRushException("Tên hiển thị phải có ít nhất 2 ký tự", HttpStatus.BAD_REQUEST);
            }
            this.displayName = newDisplayName;
        }

        if (newAge != null) {
            if (newAge < 0 || newAge > 120) {
                throw new TicketRushException("Tuổi phải nằm trong khoảng từ 0 đến 120", HttpStatus.BAD_REQUEST);
            }
            this.age = newAge;
        }

        this.gender = gender;
    }
}