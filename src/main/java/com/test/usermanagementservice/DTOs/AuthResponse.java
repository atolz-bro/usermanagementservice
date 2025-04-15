package com.test.usermanagementservice.DTOs;

public class AuthResponse {
    String jwtToken;

    public AuthResponse(String token) {
        this.jwtToken = token;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}
