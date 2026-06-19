
package com.drone.backend.dto.response;

import com.drone.backend.entity.User;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private Integer level;
    private Long experience;
    private Integer coins;
    private String selectedDrone;
    private Integer totalGames;
    private Float totalFlightTime;
    private Float totalDistance;
    private Integer totalCrashes;
    private Integer locationsDiscovered;
    private Float maxAltitude;
    private Float maxSpeed;

    public static UserResponse from(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());
        r.setAvatarUrl(u.getAvatarUrl());
        r.setLevel(u.getLevel());
        r.setExperience(u.getExperience());
        r.setCoins(u.getCoins());
        r.setSelectedDrone(u.getSelectedDrone());
        r.setTotalGames(u.getTotalGames());
        r.setTotalFlightTime(u.getTotalFlightTime());
        r.setTotalDistance(u.getTotalDistance());
        r.setTotalCrashes(u.getTotalCrashes());
        r.setLocationsDiscovered(u.getLocationsDiscovered());
        r.setMaxAltitude(u.getMaxAltitude());
        r.setMaxSpeed(u.getMaxSpeed());
        return r;
    }
}