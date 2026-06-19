
package com.drone.backend.dto.response;

import com.drone.backend.entity.Score;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScoreResponse {
    private Long id;
    private String username;
    private String category;
    private Float value;
    private String droneName;
    private LocalDateTime achievedAt;

    public static ScoreResponse from(Score s) {
        ScoreResponse r = new ScoreResponse();
        r.setId(s.getId());
        r.setUsername(s.getUser().getUsername());
        r.setCategory(s.getCategory().name());
        r.setValue(s.getValue());
        r.setDroneName(s.getDroneName());
        r.setAchievedAt(s.getAchievedAt());
        return r;
    }
}