package com.community.volunteer_system.controller;

import com.community.volunteer_system.model.*;
import com.community.volunteer_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVolunteers", userRepository.countByRole(Role.VOLUNTEER));
        stats.put("totalOrganizers", userRepository.countByRole(Role.ORGANIZER));
        stats.put("totalEvents", eventRepository.count());
        return ResponseEntity.ok(stats);
    }

    // --- SUPERIOR DRILL-DOWN ENDPOINTS ---

    @GetMapping("/users/{role}")
    public List<User> getUsersByRole(@PathVariable Role role) {
        return userRepository.findByRole(role);
    }

    @GetMapping("/user/{id}/details")
    public ResponseEntity<User> getUserDetails(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/approve-organizer/{id}")
    public ResponseEntity<?> approveOrganizer(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setApproved(true);
        userRepository.save(user);
        return ResponseEntity.ok("Organizer Approved");
    }
}