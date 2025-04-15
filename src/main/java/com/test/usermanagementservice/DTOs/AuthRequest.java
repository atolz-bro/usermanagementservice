package com.test.usermanagementservice.DTOs;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthRequest {

    String username;
    String password;

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
