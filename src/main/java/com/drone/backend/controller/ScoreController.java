
package com.drone.backend.controller;

import com.drone.backend.dto.response.*;
import com.drone.backend.entity.User;
import com.drone.backend.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;


    @GetMapping("/leaderboard/{category}")
    public ResponseEntity<LeaderboardResponse> getLeaderboard(
            @PathVariable String category) {
        return ResponseEntity.ok(scoreService.getLeaderboard(category));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ScoreResponse>> getMyScores(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(scoreService.getUserScores(user.getId()));
    }
}