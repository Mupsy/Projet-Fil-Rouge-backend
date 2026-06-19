
package com.drone.backend.repository;

import com.drone.backend.entity.Score;
import com.drone.backend.entity.Score.ScoreCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    @Query("SELECT s FROM Score s WHERE s.category = :cat ORDER BY s.value DESC")
    List<Score> findTopByCategory(
            @Param("cat") ScoreCategory category,
            Pageable pageable
    );

    @Query("SELECT s FROM Score s WHERE s.user.id = :uid AND s.category = :cat " +
            "ORDER BY s.value DESC")
    Optional<Score> findBestByUserAndCategory(
            @Param("uid") Long userId,
            @Param("cat") ScoreCategory category
    );

    List<Score> findByUserIdOrderByAchievedAtDesc(Long userId);

    @Query("SELECT COUNT(DISTINCT s.user.id) + 1 FROM Score s " +
            "WHERE s.category = :cat AND s.value > " +
            "(SELECT MAX(s2.value) FROM Score s2 WHERE s2.user.id = :uid AND s2.category = :cat)")
    Long findRankByUserAndCategory(
            @Param("uid") Long userId,
            @Param("cat") ScoreCategory category
    );
}