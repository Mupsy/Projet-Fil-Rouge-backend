
package com.drone.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "drone_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DroneSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String droneName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Builder.Default
    private Float durationMinutes = 0f;
    @Builder.Default
    private Float distanceKm = 0f;
    @Builder.Default
    private Float maxAltitude = 0f;
    @Builder.Default
    private Float maxSpeed = 0f;
    @Builder.Default
    private Integer crashes = 0;
    @Builder.Default
    private Integer locationsDiscovered = 0;
    @Builder.Default
    private Long experienceGained = 0L;
    @Builder.Default
    private Integer coinsGained = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    public enum SessionStatus {
        ACTIVE, COMPLETED, CRASHED
    }
}
