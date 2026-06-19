
package com.drone.backend.service;

import com.drone.backend.dto.request.EndSessionRequest;
import com.drone.backend.dto.response.*;
import com.drone.backend.entity.*;
import com.drone.backend.entity.Score.ScoreCategory;
import com.drone.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final UserRepository        userRepository;
    private final DroneSessionRepository sessionRepository;
    private final ScoreRepository       scoreRepository;

    private static final long XP_PER_KM       = 50L;
    private static final long XP_PER_MINUTE   = 10L;
    private static final long XP_PER_LOCATION = 100L;
    private static final int  COINS_PER_KM    = 5;

    @Transactional
    public DroneSession startSession(Long userId, String droneName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User introuvable"));

        DroneSession session = DroneSession.builder()
                .user(user)
                .droneName(droneName)
                .build();

        return sessionRepository.save(session);
    }

    @Transactional
    public UserResponse endSession(Long userId, Long sessionId,
                                   EndSessionRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User introuvable"));

        DroneSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        long xp = 0L;
        xp += Math.round(req.getDistanceKm()       * XP_PER_KM);
        xp += Math.round(req.getDurationMinutes()   * XP_PER_MINUTE);
        xp += req.getLocationsDiscovered()          * XP_PER_LOCATION;

        int coins = Math.round(req.getDistanceKm() * COINS_PER_KM);

        session.setEndedAt(LocalDateTime.now());
        session.setDurationMinutes(req.getDurationMinutes());
        session.setDistanceKm(req.getDistanceKm());
        session.setMaxAltitude(req.getMaxAltitude());
        session.setMaxSpeed(req.getMaxSpeed());
        session.setCrashes(req.getCrashes());
        session.setLocationsDiscovered(req.getLocationsDiscovered());
        session.setExperienceGained(xp);
        session.setCoinsGained(coins);
        session.setStatus(DroneSession.SessionStatus.COMPLETED);
        sessionRepository.save(session);

        user.setTotalGames(user.getTotalGames() + 1);
        user.setExperience(user.getExperience() + xp);
        user.setCoins(user.getCoins() + coins);
        user.setTotalFlightTime(user.getTotalFlightTime() + req.getDurationMinutes());
        user.setTotalDistance(user.getTotalDistance() + req.getDistanceKm());
        user.setTotalCrashes(user.getTotalCrashes() + req.getCrashes());
        user.setLocationsDiscovered(
                user.getLocationsDiscovered() + req.getLocationsDiscovered());
        if (req.getMaxAltitude() > user.getMaxAltitude())
            user.setMaxAltitude(req.getMaxAltitude());
        if (req.getMaxSpeed() > user.getMaxSpeed())
            user.setMaxSpeed(req.getMaxSpeed());

        int newLevel = (int) Math.sqrt(user.getExperience() / 100.0) + 1;
        user.setLevel(newLevel);

        userRepository.save(user);

        saveScoreIfBest(user, session, ScoreCategory.DISTANCE,
                req.getDistanceKm());
        saveScoreIfBest(user, session, ScoreCategory.FLIGHT_TIME,
                req.getDurationMinutes());
        saveScoreIfBest(user, session, ScoreCategory.MAX_ALTITUDE,
                req.getMaxAltitude());
        saveScoreIfBest(user, session, ScoreCategory.MAX_SPEED,
                req.getMaxSpeed());
        saveScoreIfBest(user, session, ScoreCategory.LOCATIONS,
                (float) req.getLocationsDiscovered());

        scoreRepository.findBestByUserAndCategory(userId, ScoreCategory.EXPERIENCE)
                .ifPresentOrElse(
                        s -> {
                            s.setValue(user.getExperience().floatValue());
                            scoreRepository.save(s);
                        },
                        () -> scoreRepository.save(Score.builder()
                                .user(user).session(session)
                                .category(ScoreCategory.EXPERIENCE)
                                .value(user.getExperience().floatValue())
                                .droneName(session.getDroneName())
                                .build())
                );

        return UserResponse.from(user);
    }

    private void saveScoreIfBest(User user, DroneSession session,
                                 ScoreCategory cat, Float value) {
        if (value == null || value <= 0) return;

        scoreRepository.findBestByUserAndCategory(user.getId(), cat)
                .ifPresentOrElse(
                        existing -> {
                            if (value > existing.getValue()) {
                                existing.setValue(value);
                                existing.setSession(session);
                                existing.setDroneName(session.getDroneName());
                                scoreRepository.save(existing);
                            }
                        },
                        () -> scoreRepository.save(Score.builder()
                                .user(user).session(session)
                                .category(cat).value(value)
                                .droneName(session.getDroneName())
                                .build())
                );
    }


    public LeaderboardResponse getLeaderboard(String categoryStr) {
        ScoreCategory cat = ScoreCategory.valueOf(categoryStr.toUpperCase());
        List<Score> scores = scoreRepository.findTopByCategory(
                cat, PageRequest.of(0, 50)
        );

        List<LeaderboardResponse.LeaderboardEntry> entries = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            Score s = scores.get(i);
            entries.add(new LeaderboardResponse.LeaderboardEntry(
                    i + 1,
                    s.getUser().getUsername(),
                    s.getValue(),
                    s.getDroneName()
            ));
        }
        return new LeaderboardResponse(cat.name(), entries);
    }


    public List<ScoreResponse> getUserScores(Long userId) {
        return scoreRepository.findByUserIdOrderByAchievedAtDesc(userId)
                .stream()
                .map(ScoreResponse::from)
                .toList();
    }
}