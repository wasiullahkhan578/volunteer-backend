package com.community.volunteer_system.service;

import com.community.volunteer_system.model.*;
import com.community.volunteer_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private EmailService emailService;

    // --- 1. ADMIN NOTIFICATIONS ---

    public void notifyAdminOfNewEvent(Event event) {
        userRepository.findByRole(Role.ADMIN).stream().findFirst().ifPresent(admin -> {
            sendInAppAndEmail(admin,
                    "New Event Tracking: " + event.getTitle(),
                    "Organizer " + event.getOrganizer().getFirstName() + " created a new mission.");
        });
    }

    public void notifyAdminOfOrganizerRegistration(User organizer) {
        userRepository.findByRole(Role.ADMIN).stream().findFirst().ifPresent(admin -> {
            Notification note = Notification.builder()
                    .recipient(admin)
                    .title("Organizer Approval Required")
                    .message(organizer.getFirstName() + " " + organizer.getLastName() + " is waiting for authorization.")
                    .type(NotificationType.SYSTEM)
                    .priority(true)
                    .build();
            notificationRepository.save(note);
        });
    }

    // --- 2. ORGANIZER NOTIFICATIONS & APPROVAL ---

    public void notifyOrganizerOfSignup(User organizer, User volunteer, String eventTitle) {
        sendInAppAndEmail(organizer,
                "New Volunteer Signup",
                volunteer.getFirstName() + " has requested to join: " + eventTitle);
    }

    @Transactional
    public void markOrganizerAsApproved(Long userId) {
        User organizer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        organizer.setApproved(true);
        userRepository.save(organizer);

        emailService.sendApprovalNotification(organizer.getEmail(), organizer.getFirstName());
        sendInAppAndEmail(organizer, "System Authorization", "Your organizer account has been verified by the Admin.");
    }

    // --- 3. VOLUNTEER NOTIFICATIONS & FEEDBACK ---

    public void notifyVolunteerOfApproval(User volunteer, String eventTitle, ParticipationStatus status) {
        String title = (status == ParticipationStatus.ACCEPTED) ? "Mission Accepted!" : "Mission Update";
        String message = (status == ParticipationStatus.ACCEPTED)
                ? "Pack your bags! You have been accepted for the mission: " + eventTitle
                : "Regarding your request for " + eventTitle + ", the status is now: " + status;

        sendInAppAndEmail(volunteer, title, message);
    }

    public void notifyVolunteersToRateEvent(Event event) {
        List<Participation> participants = participationRepository.findByEventId(event.getId());

        List<User> acceptedVolunteers = participants.stream()
                .filter(p -> p.getStatus() == ParticipationStatus.ACCEPTED)
                .map(Participation::getUser)
                .collect(Collectors.toList());

        for (User volunteer : acceptedVolunteers) {
            sendInAppAndEmail(volunteer,
                    "Mission Completed: Provide Feedback",
                    "How was your experience with '" + event.getTitle() + "'? Rate it now.");
        }
    }

    // --- 4. DATA RETRIEVAL & UPDATE LOGIC ---

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId).size();
    }

    @Transactional
    public void markSingleAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(note -> {
            note.setRead(true);
            notificationRepository.save(note);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // --- HELPER: SAFE SEND ---
    public void sendInAppAndEmail(User recipient, String title, String message) {
        Notification note = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(NotificationType.UPDATE)
                .build();

        notificationRepository.save(note);

        try {
            emailService.sendSimpleNotification(recipient.getEmail(), title, message);
        } catch (Exception e) {
            System.err.println("Email failed to send: " + e.getMessage());
        }
    }
}