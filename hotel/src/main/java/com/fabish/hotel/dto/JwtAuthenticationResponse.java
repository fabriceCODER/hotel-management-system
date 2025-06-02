package com.fabish.hotel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    
    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}