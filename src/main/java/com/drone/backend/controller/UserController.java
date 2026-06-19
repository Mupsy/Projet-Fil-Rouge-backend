
package com.drone.backend.controller;

import com.drone.backend.dto.request.EndSessionRequest;
import com.drone.backend.dto.response.UserResponse;
import com.drone.backend.entity.User;
import com.drone.backend.service.ScoreService;
import com.drone.backend.repository.DroneSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ScoreService           scoreService;
    private final DroneSessionRepository sessionRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/sessions/start")
    public ResponseEntity<Map<String, Long>> startSession(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {
        String droneName = body.getOrDefault("droneName", "Phantom Scout");
        var session = scoreService.startSession(user.getId(), droneName);
        return ResponseEntity.ok(Map.of("sessionId", session.getId()));
    }

    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<UserResponse> endSession(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId,
            @RequestBody EndSessionRequest req) {
        return ResponseEntity.ok(
                scoreService.endSession(user.getId(), sessionId, req)
        );
    }

    @GetMapping("/me/sessions")
    public ResponseEntity<?> getMySessions(
            @AuthenticationPrincipal User user) {
        var sessions = sessionRepository.findByUserIdOrderByStartedAtDesc(
                user.getId(), PageRequest.of(0, 10)
        );
        return ResponseEntity.ok(sessions);
    }
}