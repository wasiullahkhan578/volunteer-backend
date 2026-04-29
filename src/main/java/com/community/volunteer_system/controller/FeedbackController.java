package com.community.volunteer_system.controller;

import com.community.volunteer_system.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "http://localhost:5173")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/submit")
    public ResponseEntity<?> postFeedback(@RequestParam Long eventId,
                                          @RequestParam Long volunteerId,
                                          @RequestParam int rating,
                                          @RequestParam String comment) {
        return ResponseEntity.ok(feedbackService.submitFeedback(eventId, volunteerId, rating, comment));
    }
}