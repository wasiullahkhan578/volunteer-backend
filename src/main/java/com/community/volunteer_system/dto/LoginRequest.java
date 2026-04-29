package com.community.volunteer_system.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private String role;
}