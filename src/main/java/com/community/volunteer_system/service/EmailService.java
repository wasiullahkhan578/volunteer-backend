package com.community.volunteer_system.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private final String FROM_EMAIL = "er.wasiullah.khan@gmail.com";

    // --- MILESTONE 1: Registration Verification ---
    public void sendVerificationEmail(String toEmail, String firstName, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String verificationLink = "http://localhost:8080/api/auth/verify?token=" + token;

            String htmlContent = String.format(
                    "<div style='font-family: sans-serif; background-color: #0f172a; color: #ffffff; padding: 40px; border-radius: 20px;'>" +
                            "  <h1 style='color: #2dd4bf;'>Volunteer<span style='color: #ffffff;'>Hub</span><span style='color: #eab308;'>.</span></h1>" +
                            "  <h2 style='font-size: 24px;'>Verify Your Email</h2>" +
                            "  <p style='color: #94a3b8; line-height: 1.6;'>Hi %s, almost there! Please click the button below to verify your account and join our mission-driven community.</p>" +
                            "  <div style='margin-top: 30px;'>" +
                            "    <a href='%s' style='background-color: #2dd4bf; color: #0f172a; padding: 14px 28px; text-decoration: none; border-radius: 10px; font-weight: bold; display: inline-block;'>Verify Email Address</a>" +
                            "  </div>" +
                            "  <p style='margin-top: 40px; font-size: 12px; color: #475569;'>If you didn't create an account, you can safely ignore this email.</p>" +
                            "</div>",
                    firstName,
                    verificationLink
            );

            helper.setText(htmlContent, true);
            helper.setTo(toEmail);
            helper.setSubject("Verify your VolunteerHub Account");
            helper.setFrom(FROM_EMAIL);

            mailSender.send(mimeMessage);
            System.out.println("Verification Email sent to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new IllegalStateException("Failed to send verification email");
        }
    }

    // --- MILESTONE 1: Password Recovery ---
    public void sendResetPasswordEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - VolunteerHub");

            String resetLink = "http://localhost:5173/reset-password?token=" + token;

            String htmlContent = String.format(
                    "<div style='font-family: sans-serif; background-color: #0f172a; color: #ffffff; padding: 40px; border-radius: 20px;'>" +
                            "  <h2 style='color: #2dd4bf;'>Password Reset Request</h2>" +
                            "  <p style='color: #94a3b8;'>We received a request to reset your password for your VolunteerHub account.</p>" +
                            "  <div style='margin-top: 30px;'>" +
                            "    <a href='%s' style='background-color: #2dd4bf; color: #0f172a; padding: 14px 28px; text-decoration: none; border-radius: 10px; font-weight: bold; display: inline-block;'>Reset My Password</a>" +
                            "  </div>" +
                            "  <p style='margin-top: 30px; font-size: 12px; color: #475569;'>This link will expire in 1 hour. If you did not request this, please ignore this email.</p>" +
                            "</div>",
                    resetLink
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("Reset email sent successfully to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("SMTP Error: " + e.getMessage());
            throw new RuntimeException("Failed to send reset email: " + e.getMessage());
        }
    }

    // --- NEW: MILESTONE 3 Organizer Approval Notification ---
    /**
     * Notifies an Organizer that their account has been authorized by the Admin.
     * This resolves the 'Cannot resolve method' error in NotificationService.
     */
    public void sendApprovalNotification(String toEmail, String firstName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String loginLink = "http://localhost:5173/login";

            String htmlContent = String.format(
                    "<div style='font-family: sans-serif; background-color: #0f172a; color: #ffffff; padding: 40px; border-radius: 20px; border: 1px solid #2dd4bf;'>" +
                            "  <h1 style='color: #2dd4bf;'>Volunteer<span style='color: #ffffff;'>Hub</span><span style='color: #eab308;'>.</span></h1>" +
                            "  <h2 style='font-size: 24px; color: #2dd4bf;'>Access Authorized!</h2>" +
                            "  <p style='color: #94a3b8; line-height: 1.6;'>Hi %s, great news!</p>" +
                            "  <p style='color: #ffffff; font-weight: 500;'>Your Organizer account has been officially verified and authorized by our Superior Admin panel.</p>" +
                            "  <p style='color: #94a3b8; line-height: 1.6;'>You now have system-wide access to create missions, manage volunteers, and track community impact.</p>" +
                            "  <div style='margin-top: 30px;'>" +
                            "    <a href='%s' style='background-color: #2dd4bf; color: #0f172a; padding: 14px 28px; text-decoration: none; border-radius: 10px; font-weight: bold; display: inline-block;'>Access Control Center</a>" +
                            "  </div>" +
                            "  <p style='margin-top: 40px; font-size: 12px; color: #475569;'>Account Type: ORGANIZER | Authorized Sector Participant</p>" +
                            "</div>",
                    firstName,
                    loginLink
            );

            helper.setText(htmlContent, true);
            helper.setTo(toEmail);
            helper.setSubject("Organizer Account Authorized - VolunteerHub");
            helper.setFrom(FROM_EMAIL);

            mailSender.send(mimeMessage);
            System.out.println("Approval Notification sent to Organizer: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send approval email: " + e.getMessage());
        }
    }

    // --- MILESTONE 2: New Simple Notification (For Event Cancellations) ---
    public void sendSimpleNotification(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        System.out.println("Sending simple notification to: " + to);
        mailSender.send(message);
    }
}