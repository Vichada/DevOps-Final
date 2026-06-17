package com.devops.idcard.service;

import com.devops.idcard.model.Profile;
import com.devops.idcard.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<Profile> getAllProfiles() { return profileRepository.findAll(); }
    public Profile getProfileById(Long id) { return profileRepository.findById(id).orElse(null); }
    public List<Profile> searchByName(String name) { return profileRepository.findByFullNameContainingIgnoreCase(name); }
    public void deleteProfile(Long id) { profileRepository.deleteById(id); }

    public Profile saveProfile(Profile profile) {
        // Generate stable public UUID for QR codes if missing
        if (profile.getUuid() == null || profile.getUuid().isEmpty()) {
            profile.setUuid(UUID.randomUUID().toString());
        }

        // Generate custom sequence registration code (YEAR-DEPT-###) if missing
        if (profile.getRegistrationNumber() == null || profile.getRegistrationNumber().isEmpty()) {
            profile.setRegistrationNumber(generateCustomSequence(profile.getDepartment()));
        }
        
        return profileRepository.save(profile);
    }

    public List<Profile> saveBatchProfiles(List<Profile> profiles) {
        List<Profile> savedBatch = new ArrayList<>();
        for (Profile p : profiles) {
            savedBatch.add(saveProfile(p));
        }
        return savedBatch;
    }

    private String generateCustomSequence(String department) {
        String deptCode = (department != null && !department.isEmpty()) 
                ? department.replaceAll("\\s+", "").toUpperCase() : "GEN";
        if (deptCode.length() > 4) deptCode = deptCode.substring(0, 4);
        
        int currentYear = Year.now().getValue();
        int randomSerial = 100 + new Random().nextInt(900);
        
        return currentYear + "-" + deptCode + "-" + randomSerial;
    }
}
