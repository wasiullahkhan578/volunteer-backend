package com.community.volunteer_system.controller;

import com.community.volunteer_system.model.*;
import com.community.volunteer_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    /**
     * Unified Profile Endpoint
     * Returns User details + Impact Stats for Volunteers
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).map(user -> {
            // Create a map to hold both user data and stats
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("approved", user.isApproved());
            response.put("mobile", user.getMobile());
            response.put("skills", user.getSkills());

            // If it's a Volunteer, calculate impact metrics for the dashboard
            if (user.getRole() == Role.VOLUNTEER) {
                List<Participation> history = participationRepository.findByUserId(user.getId());

                // INTEGRATED: ONLY count accepted ones for the "Missions Joined" card
                long totalEvents = history.stream()
                        .filter(p -> p.getStatus() == ParticipationStatus.ACCEPTED)
                        .count();

                int totalDaysAttended = history.stream()
                        .mapToInt(Participation::getDaysAttended)
                        .sum();

                response.put("totalEventsAttended", totalEvents);
                response.put("totalHoursContributed", totalDaysAttended * 4); // 4 hrs/day logic
                response.put("averageAttendanceRate", calculateAvgAttendance(history));
            }

            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates the user profile with role-specific logic.
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedData, @AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).map(user -> {
            user.setFirstName(updatedData.getFirstName());
            user.setLastName(updatedData.getLastName());
            user.setMobile(updatedData.getMobile());

            if (Role.VOLUNTEER.equals(user.getRole())) {
                user.setSkills(updatedData.getSkills());
                user.setVolunteerExperience(updatedData.getVolunteerExperience());
            } else if (Role.ORGANIZER.equals(user.getRole())) {
                user.setAadhar(updatedData.getAadhar());
                user.setOrganizerExperience(updatedData.getOrganizerExperience());
            }

            userRepository.save(user);
            return ResponseEntity.ok("Profile synchronized successfully!");
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- ADMIN SECTION ---

    @GetMapping("/admin/pending-organizers")
    public ResponseEntity<List<User>> getPendingOrganizers() {
        List<User> pending = userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == Role.ORGANIZER && !user.isApproved())
                .collect(Collectors.toList());
        return ResponseEntity.ok(pending);
    }

    @PutMapping("/admin/approve/{id}")
    public ResponseEntity<String> approveOrganizer(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            user.setApproved(true);
            userRepository.save(user);
            return ResponseEntity.ok("Organizer approved successfully!");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Long>> getAdminStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalVolunteers", userRepository.countByRole(Role.VOLUNTEER));
        stats.put("totalOrganizers", userRepository.countByRole(Role.ORGANIZER));
        stats.put("totalEvents", eventRepository.count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/admin/users/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userRepository.findByRole(role));
    }

    // --- HELPER METHODS ---

    private double calculateAvgAttendance(List<Participation> history) {
        if (history == null || history.isEmpty()) return 0.0;

        double totalRate = 0;
        int eligibleMissions = 0;

        for (Participation p : history) {
            if (p.getEvent() != null && p.getEvent().getTotalDays() > 0) {
                double rate = ((double) p.getDaysAttended() / p.getEvent().getTotalDays()) * 100;
                totalRate += rate;
                eligibleMissions++;
            }
        }

        if (eligibleMissions == 0) return 0.0;
        return Math.round((totalRate / eligibleMissions) * 10.0) / 10.0;
    }
}