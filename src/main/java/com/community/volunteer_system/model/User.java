package com.community.volunteer_system.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String mobile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // --- Role Specific Fields ---
    private String skills;
    private String volunteerExperience;
    private String aadhar;
    private String organizerExperience;

    // --- Status & Security Fields ---
    private boolean enabled = false;
    private String verificationToken;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    @Column(nullable = false)
    private boolean approved = false;

    // --- MILESTONE 3: Impact & Analytics (Superior Admin View) ---
    // These fields allow the Admin to see real-time participation metrics
    private int totalEventsAttended = 0;
    private double totalHoursContributed = 0.0;

    // For Volunteer Selection: Organizers can see this before accepting a request
    private double averageAttendanceRate = 0.0;

    // For Organizer Trust Score: Average of feedback ratings
    private double organizerRating = 0.0;

    // --- UserDetails Interface Methods ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (role == Role.VOLUNTEER || role == Role.ADMIN) {
            return this.enabled;
        }
        return this.enabled && this.approved;
    }
}