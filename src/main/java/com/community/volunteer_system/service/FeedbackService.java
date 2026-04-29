package com.community.volunteer_system.service;

import com.community.volunteer_system.model.*;
import com.community.volunteer_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public String submitFeedback(Long eventId, Long volunteerId, int rating, String comment) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.isCompleted()) {
            throw new RuntimeException("Feedback can only be provided for completed events.");
        }

        // Verify participation and attendance
        Participation p = participationRepository.findByUserIdAndEventId(volunteerId, eventId)
                .orElseThrow(() -> new RuntimeException("You did not participate in this event."));

        Feedback feedback = new Feedback();
        feedback.setEvent(event);
        feedback.setVolunteer(p.getUser());
        feedback.setRating(rating);
        feedback.setComment(comment);

        feedbackRepository.save(feedback);

        // Update Organizer Trust Score
        updateOrganizerRating(event.getOrganizer().getId());

        return "Thank you for your feedback!";
    }

    private void updateOrganizerRating(Long organizerId) {
        Double avgRating = feedbackRepository.getAverageRatingForOrganizer(organizerId);
        User organizer = userRepository.findById(organizerId).orElseThrow();
        organizer.setOrganizerRating(avgRating != null ? avgRating : 0.0);
        userRepository.save(organizer);
    }
}