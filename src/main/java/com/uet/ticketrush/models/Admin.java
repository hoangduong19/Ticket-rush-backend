package com.uet.ticketrush.models;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "admins")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "admin_id", updatable = false, nullable = false)
    private UUID adminId;

    @Column(name ="email", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "display_name", length = 50)
    private String displayName;
}