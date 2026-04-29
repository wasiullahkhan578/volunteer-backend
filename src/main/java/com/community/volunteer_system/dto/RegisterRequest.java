package com.community.volunteer_system.dto;

import com.community.volunteer_system.model.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String password;
    private String role;

    // Volunteer specific
    private String skills;
    private String volunteerExperience;

    // Organizer specific
    private String aadhar;
    private String organizerExperience;
}