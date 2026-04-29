package com.community.volunteer_system.controller;

import com.community.volunteer_system.dto.AttendanceRequest;
import com.community.volunteer_system.model.Attendance;
import com.community.volunteer_system.model.Participation;
import com.community.volunteer_system.repository.AttendanceRepository;
import com.community.volunteer_system.repository.ParticipationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "http://localhost:5173")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @PostMapping("/mark")
    @Transactional // Ensures data integrity for the batch update
    public ResponseEntity<?> markDailyAttendance(@RequestBody AttendanceRequest request) {
        LocalDate today = LocalDate.now();
        int processedCount = 0;

        for (AttendanceRequest.VolunteerStatus record : request.getRecords()) {

            // 1. Prevent duplicate marking for the same participation on the same day
            if (attendanceRepository.existsByParticipationIdAndAttendanceDate(record.getParticipationId(), today)) {
                continue;
            }

            // 2. Fetch Participation Participation
            Participation p = participationRepository.findById(record.getParticipationId())
                    .orElseThrow(() -> new RuntimeException("Participation ID " + record.getParticipationId() + " not found"));

            // 3. Map and Save Attendance Record
            Attendance attendance = new Attendance();
            attendance.setParticipation(p);
            attendance.setAttendanceDate(today);
            attendance.setPresent(record.isPresent());
            attendanceRepository.save(attendance);

            // 4. Update Participation Stats (Critical for Certification Logic)
            if (record.isPresent()) {
                p.setDaysAttended(p.getDaysAttended() + 1);
                participationRepository.save(p);
            }

            processedCount++;
        }

        return ResponseEntity.ok(Map.of(
                "message", "Attendance synchronized successfully",
                "date", today.toString(),
                "recordsProcessed", processedCount
        ));
    }
}