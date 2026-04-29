package com.community.volunteer_system.service;

import com.community.volunteer_system.model.*;
import com.community.volunteer_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    // --- 1. Event Management ---

    @Transactional
    public Event createEvent(Event event, User organizer) {
        event.setOrganizer(organizer);
        event.setRegistrationOpen(true);
        event.setCompleted(false);

        Event savedEvent = eventRepository.save(event);

        notificationService.notifyAdminOfNewEvent(savedEvent);

        List<User> volunteers = userRepository.findByRole(Role.VOLUNTEER);
        for (User volunteer : volunteers) {
            notificationService.sendInAppAndEmail(
                    volunteer,
                    "New Mission Alert",
                    "Organizer " + organizer.getFirstName() + " has posted: " + event.getTitle()
            );
        }

        return savedEvent;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission not found with ID: " + id));
    }

    @Transactional
    public String closeRegistration(Long eventId, User organizer) {
        Event event = getEventById(eventId);

        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new RuntimeException("Unauthorized: Only the organizer can close registration");
        }

        event.setRegistrationOpen(false);
        eventRepository.save(event);
        return "Registration closed for: " + event.getTitle();
    }

    @Transactional
    public String markEventCompleted(Long eventId) {
        Event event = getEventById(eventId);

        event.setCompleted(true);
        event.setRegistrationOpen(false);
        eventRepository.save(event);

        notificationService.notifyVolunteersToRateEvent(event);
        return "Event marked as completed. Feedback requests sent.";
    }

    // --- 2. Organizer Selection & Status Updates ---

    @Transactional
    public String updateParticipationStatus(Long participationId, ParticipationStatus newStatus) {
        Participation p = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation record not found"));

        p.setStatus(newStatus);
        participationRepository.save(p);

        // OPTIMIZED: Use Repository count instead of Stream
        if (newStatus == ParticipationStatus.ACCEPTED) {
            Event event = p.getEvent();
            long acceptedCount = participationRepository.countByEventIdAndStatus(event.getId(), ParticipationStatus.ACCEPTED);

            if (acceptedCount >= event.getRequiredVolunteers()) {
                event.setRegistrationOpen(false);
                eventRepository.save(event);
            }
        }

        notificationService.sendInAppAndEmail(
                p.getUser(),
                "Mission Application Update",
                "Your request to join '" + p.getEvent().getTitle() + "' has been " + newStatus.name()
        );

        return "Volunteer status updated to " + newStatus;
    }

    // --- 3. Volunteer Participation & Withdrawals ---

    @Transactional
    public String signUpForEvent(User volunteer, Long eventId) {
        if (participationRepository.existsByUserIdAndEventId(volunteer.getId(), eventId)) {
            throw new RuntimeException("Already requested to join this event");
        }

        Event event = getEventById(eventId);
        if (!event.isRegistrationOpen()) {
            throw new RuntimeException("Registration is currently closed for this mission");
        }

        Participation p = Participation.builder()
                .user(volunteer)
                .event(event)
                .status(ParticipationStatus.PENDING)
                .daysAttended(0)
                .build();

        participationRepository.save(p);
        notificationService.notifyOrganizerOfSignup(event.getOrganizer(), volunteer, event.getTitle());

        return "Application sent! Waiting for organizer approval.";
    }

    @Transactional
    public String cancelParticipation(User volunteer, Long eventId) {
        Participation participation = participationRepository.findByUserIdAndEventId(volunteer.getId(), eventId)
                .orElseThrow(() -> new RuntimeException("Participation record not found"));

        participation.setStatus(ParticipationStatus.CANCELLED);
        participationRepository.save(participation);

        notificationService.sendInAppAndEmail(
                participation.getEvent().getOrganizer(),
                "Volunteer Withdrawal",
                volunteer.getFirstName() + " " + volunteer.getLastName() + " has withdrawn from " + participation.getEvent().getTitle()
        );

        return "Participation cancelled. Organizer notified.";
    }

    // --- 4. Analytics & Stats (High Performance Queries) ---

    public Map<String, Object> getGlobalSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", eventRepository.count());
        stats.put("liveEvents", eventRepository.countByCompletedFalseAndRegistrationOpenTrue());
        return stats;
    }

    public Map<String, Object> getOrganizerStats(Long organizerId) {
        Map<String, Object> stats = new HashMap<>();

        // OPTIMIZED: Using direct Repository counts
        long total = eventRepository.countByOrganizerId(organizerId);
        long live = eventRepository.countByOrganizerIdAndCompletedFalseAndRegistrationOpenTrue(organizerId);
        long completed = eventRepository.countByOrganizerIdAndCompletedTrue(organizerId);

        stats.put("totalEvents", total);
        stats.put("liveEvents", live);
        stats.put("completedCount", total > 0 ? (completed * 100 / total) : 0);

        return stats;
    }

    public List<Event> getEventsByOrganizer(Long organizerId) {
        // Optimized: Returns latest events first
        return eventRepository.findByOrganizerIdOrderByIdDesc(organizerId);
    }

    public List<Participation> getPendingParticipationsForOrganizer(Long organizerId) {
        // Optimized: Scoped query for pending requests
        return participationRepository.findByEventOrganizerIdAndStatus(organizerId, ParticipationStatus.PENDING);
    }

    // --- 5. Certification Logic (75% Attendance) ---

    public Map<String, Object> getCertificateEligibility(Long participationId) {
        Participation p = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation record not found"));

        if (!p.getEvent().isCompleted()) {
            throw new RuntimeException("Certificates are issued only after event completion");
        }

        int daysPresent = p.getDaysAttended();
        int totalDays = p.getEvent().getTotalDays();
        double percentage = (totalDays > 0) ? ((double) daysPresent / totalDays) * 100 : 0;

        Map<String, Object> report = new HashMap<>();
        report.put("eligible", percentage >= 75.0);
        report.put("attendanceRecord", Math.round(percentage * 10.0) / 10.0 + "%");

        return report;
    }

    public boolean isEligibleForCertificate(Long userId, Long eventId) {
        Participation p = participationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new RuntimeException("No record found"));

        if (p.getEvent().getTotalDays() == 0) return false;
        double attendanceRatio = (double) p.getDaysAttended() / p.getEvent().getTotalDays();
        return attendanceRatio >= 0.75;
    }
    public List<Participation> getVolunteerHistory(Long userId) {
        // We use the participationRepository to find all records linked to this user
        return participationRepository.findByUserId(userId);
    }


}