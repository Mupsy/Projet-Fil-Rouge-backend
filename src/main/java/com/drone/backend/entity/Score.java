
package com.drone.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "scores",
        indexes = {
                @Index(name = "idx_score_user",     columnList = "user_id"),
                @Index(name = "idx_score_category", columnList = "category"),
                @Index(name = "idx_score_value",    columnList = "value DESC")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private DroneSession session;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ScoreCategory category;

    @Column(nullable = false)
    private Float value;

    @Column(length = 50)
    private String droneName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime achievedAt;

    public enum ScoreCategory {
        DISTANCE,
        FLIGHT_TIME,
        MAX_ALTITUDE,
        MAX_SPEED,
        LOCATIONS,
        EXPERIENCE
    }
}