package com.community.volunteer_system.service;

import com.community.volunteer_system.dto.LoginRequest;
import com.community.volunteer_system.dto.LoginResponse;
import com.community.volunteer_system.dto.RegisterRequest;
import com.community.volunteer_system.model.Role;
import com.community.volunteer_system.model.User;
import com.community.volunteer_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService; // NEW: Integrated for real-mail delivery

    @Autowired
    private NotificationService notificationService; // NEW: Integrated for Admin alerts

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Role userRole;
        try {
            userRole = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Role specified: " + request.getRole());
        }

        // Milestone 3: Set initial states based on role
        boolean requiresApproval = userRole == Role.ORGANIZER;

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .mobile(request.getMobile())
                .role(userRole)
                .enabled(false) // Must verify email first
                .approved(!requiresApproval) // Organizers start as unapproved
                .verificationToken(UUID.randomUUID().toString())
                .build();

        User savedUser = userRepository.save(user);

        // 1. Send Verification Email
        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getVerificationToken()
        );

        // 2. If Organizer, Alert Admin for Superior Audit
        if (requiresApproval) {
            notificationService.notifyAdminOfOrganizerRegistration(savedUser);
        }

        return "Registration successful! Please check your email to verify your account.";
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Milestone 3: Role-specific gatekeeping
        if (!user.isEnabled()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }

        if (user.getRole() == Role.ORGANIZER && !user.isApproved()) {
            throw new RuntimeException("Your account is pending Superior Admin approval.");
        }

        String jwtToken = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(jwtToken)
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .approved(user.isApproved()) // Send approval status to frontend
                .message("Login successful")
                .build();
    }

    @Transactional
    public void verifyUser(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    @Transactional
    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        // Integrated Milestone 3 Email Recovery
        emailService.sendResetPasswordEmail(user.getEmail(), token);
    }

    @Transactional
    public void updatePassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }
}