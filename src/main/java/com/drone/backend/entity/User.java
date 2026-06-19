
package com.drone.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 500)
    private String avatarUrl;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(nullable = false)
    @Builder.Default
    private Long experience = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Integer coins = 0;

    @Column(nullable = false)
    @Builder.Default
    private String selectedDrone = "Phantom Scout";

    @Builder.Default
    private Integer totalGames = 0;
    @Builder.Default
    private Float totalFlightTime = 0f;    
    @Builder.Default
    private Float totalDistance = 0f;      
    @Builder.Default
    private Integer totalCrashes = 0;
    @Builder.Default
    private Integer locationsDiscovered = 0;
    @Builder.Default
    private Float maxAltitude = 0f;
    @Builder.Default
    private Float maxSpeed = 0f;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DroneSession> sessions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Score> scores;
}