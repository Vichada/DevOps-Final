package com.devops.idcard.repository;

import com.devops.idcard.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    // For searching profile data as requested in the exam sheet
    List<Profile> findByFullNameContainingIgnoreCase(String name);
    
    // For checking existence of a Profile in the DB
    boolean existsByRegistrationNumber(String registrationNumber);
}
