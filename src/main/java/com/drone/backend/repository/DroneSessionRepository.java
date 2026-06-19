
package com.drone.backend.repository;

import com.drone.backend.entity.DroneSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DroneSessionRepository extends JpaRepository<DroneSession, Long> {

    List<DroneSession> findByUserIdOrderByStartedAtDesc(Long userId, Pageable pageable);

    Optional<DroneSession> findByUserIdAndStatus(
            Long userId,
            DroneSession.SessionStatus status
    );
}
