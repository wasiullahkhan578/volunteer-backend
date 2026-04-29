package com.community.volunteer_system.controller;

import com.community.volunteer_system.model.*;
import com.community.volunteer_system.repository.EventRepository;
import com.community.volunteer_system.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/volunteer")
@CrossOrigin(origins = "http://localhost:5173")
public class VolunteerController {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    // --- REAL-TIME SEARCH (Milestone 2) ---
    @GetMapping("/search")
    public List<Event> searchEvents(@RequestParam String query) {
        return eventRepository.findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(query, query);
    }

    @GetMapping("/history/{userId}")
    public List<Participation> getMyHistory(@PathVariable Long userId) {
        return eventService.getVolunteerHistory(userId);
    }

    // --- CERTIFICATION CHECK ---
    @GetMapping("/participation/{id}/eligibility")
    public ResponseEntity<?> checkCertificate(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getCertificateEligibility(id));
    }
}