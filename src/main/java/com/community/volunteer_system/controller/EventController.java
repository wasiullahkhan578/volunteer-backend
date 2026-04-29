package com.community.volunteer_system.controller;

import com.community.volunteer_system.model.Event;
import com.community.volunteer_system.model.Participation;
import com.community.volunteer_system.model.ParticipationStatus;
import com.community.volunteer_system.model.User;
import com.community.volunteer_system.repository.ParticipationRepository;
import com.community.volunteer_system.service.EventService;
import com.community.volunteer_system.repository.UserRepository;
import com.community.volunteer_system.service.NotificationService;
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
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private NotificationService notificationService;

    // 1. Get Single Event Details (Used for DrillDown and Info Modals)
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    // 2. Post a new event (Organizers only)
    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestBody Event event, @AuthenticationPrincipal UserDetails userDetails) {
        User organizer = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Organizer not found"));
        return ResponseEntity.ok(eventService.createEvent(event, organizer));
    }

    // 3. Browse all events (Find Events Page)
    @GetMapping("/all")
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // 4. Join an event (Volunteer Action)
    @PostMapping("/join/{eventId}")
    public ResponseEntity<String> joinEvent(@PathVariable Long eventId, @AuthenticationPrincipal UserDetails userDetails) {
        User volunteer = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Volunteer not found"));
        return ResponseEntity.ok(eventService.signUpForEvent(volunteer, eventId));
    }

    // 5. Cancel participation (Withdrawal)
    @PostMapping("/cancel/{eventId}")
    public ResponseEntity<String> cancelEvent(@PathVariable Long eventId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(eventService.cancelParticipation(user, eventId));
    }

    // 6. Organizer: Get all pending sign-ups
    @GetMapping("/organizer/pending-volunteers")
    public ResponseEntity<List<Participation>> getPendingVolunteers(@AuthenticationPrincipal UserDetails userDetails) {
        User organizer = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Organizer not found"));
        return ResponseEntity.ok(eventService.getPendingParticipationsForOrganizer(organizer.getId()));
    }

    // 7. Organizer: Approve Volunteer (Triggers Notification Handshake)
    @PutMapping("/participation/{id}/approve")
    public ResponseEntity<String> approveVolunteer(@PathVariable Long id) {
        // This calls service logic: Status -> ACCEPTED + Notification -> Volunteer
        eventService.updateParticipationStatus(id, ParticipationStatus.ACCEPTED);
        return ResponseEntity.ok("Volunteer accepted into squad.");
    }

    // 8. Organizer: Deny Volunteer
    @PutMapping("/participation/{id}/deny")
    public ResponseEntity<String> denyVolunteer(@PathVariable Long id) {
        eventService.updateParticipationStatus(id, ParticipationStatus.DENIED);
        return ResponseEntity.ok("Volunteer application denied.");
    }

    // 9. Organizer: Dashboard Stats
    @GetMapping("/organizer/stats")
    public ResponseEntity<Map<String, Object>> getMyStats(@AuthenticationPrincipal UserDetails userDetails) {
        User organizer = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(eventService.getOrganizerStats(organizer.getId()));
    }

    // 10. Organizer: List of My Missions
    @GetMapping("/my-missions")
    public ResponseEntity<List<Event>> getMyMissions(@AuthenticationPrincipal UserDetails userDetails) {
        User organizer = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(eventService.getEventsByOrganizer(organizer.getId()));
    }

    // 11. Volunteer: Quest Log / History with Attendance Calculation
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getMyHistory(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Participation> history = eventService.getVolunteerHistory(user.getId());

        List<Map<String, Object>> response = history.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("status", p.getStatus());
            map.put("event", p.getEvent());
            map.put("daysAttended", p.getDaysAttended());

            // Attendance Rate logic for frontend progress bars & certificates
            int totalDays = p.getEvent().getTotalDays();
            double rate = (totalDays > 0) ? ((double) p.getDaysAttended() / totalDays) * 100 : 0;
            map.put("attendanceRate", Math.round(rate));

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}