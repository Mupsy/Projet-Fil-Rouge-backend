
package com.drone.backend.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserResponse user;
}