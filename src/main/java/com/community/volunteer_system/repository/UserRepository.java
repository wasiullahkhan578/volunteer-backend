package com.community.volunteer_system.repository;

import com.community.volunteer_system.model.User;
import com.community.volunteer_system.model.Role; // Ensure you import your Role Enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // --- MILESTONE 1: Authentication & Verification ---
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByResetToken(String token);

    // --- MILESTONE 2: Admin Analytics & Management ---

    // Returns the total count of a specific role (e.g., 42 Volunteers)
    long countByRole(Role role);

    // Returns a list of all users with a specific role
    List<User> findByRole(Role role);

    // Used for the Admin Panel to find only "Pending" or "Approved" Organizers
    List<User> findByRoleAndApproved(Role role, boolean approved);
}