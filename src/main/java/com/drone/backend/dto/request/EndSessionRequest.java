
package com.drone.backend.dto.request;

import lombok.Data;

@Data
public class EndSessionRequest {
    private Float durationMinutes;
    private Float distanceKm;
    private Float maxAltitude;
    private Float maxSpeed;
    private Integer crashes;
    private Integer locationsDiscovered;
}