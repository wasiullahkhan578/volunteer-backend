package com.community.volunteer_system.controller;

import com.community.volunteer_system.model.*;
import com.community.volunteer_system.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizer")
@CrossOrigin(origins = "http://localhost:5173")
public class OrganizerController {

    @Autowired
    private EventService eventService;

    // --- SELECTION LOGIC (Milestone 3) ---
    @PutMapping("/participation/{id}/status")
    public ResponseEntity<?> updateVolunteerStatus(@PathVariable Long id, @RequestParam ParticipationStatus status) {
        return ResponseEntity.ok(eventService.updateParticipationStatus(id, status));
    }

    // --- EVENT LIFECYCLE (Milestone 3) ---
    @PutMapping("/event/{id}/complete")
    public ResponseEntity<?> completeEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.markEventCompleted(id));
    }

    @PutMapping("/event/{id}/close-registration")
    public ResponseEntity<?> closeReg(@PathVariable Long id, @AuthenticationPrincipal User organizer) {
        return ResponseEntity.ok(eventService.closeRegistration(id, organizer));
    }
}