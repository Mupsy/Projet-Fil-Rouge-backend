
package com.drone.backend.dto.response;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
public class LeaderboardResponse {
    private String category;
    private List<LeaderboardEntry> entries;

    @Data
    @AllArgsConstructor
    public static class LeaderboardEntry {
        private int rank;
        private String username;
        private Float value;
        private String droneName;
    }
}