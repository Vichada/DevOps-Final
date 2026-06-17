package com.devops.idcard.controller;

import com.devops.idcard.model.Profile;
import com.devops.idcard.model.ProfileType;
import com.devops.idcard.service.CardMediaService;
import com.devops.idcard.service.PdfExportService;
import com.devops.idcard.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // Changed from @RestController to support views
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller // Use @Controller so redirection strings link straight to your Thymeleaf engine
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final CardMediaService cardMediaService;
    private final PdfExportService pdfExportService;
    
    private static final String UPLOAD_DIR = "uploads/";

    // Consolidated constructor matching our structured fields
    public ProfileController(ProfileService profileService, CardMediaService cardMediaService, PdfExportService pdfExportService) {
        this.profileService = profileService;
        this.cardMediaService = cardMediaService;
        this.pdfExportService = pdfExportService;
    }

    // JSON Data API Endpoint via explicit @ResponseBody annotation
    @GetMapping
    @ResponseBody 
    public List<Profile> getAllProfiles() {
        return profileService.getAllProfiles();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Profile> getProfileById(@PathVariable Long id) {
        Profile profile = profileService.getProfileById(id);
        return profile != null ? ResponseEntity.ok(profile) : ResponseEntity.notFound().build();
    }

    // Handles Dashboard Creation Submission Form and Refreshes the View
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("department") String department,
            @RequestParam("profileType") String profileType,
            @RequestParam(value = "photo", required = false) MultipartFile file) {
        try {
            Profile profile = new Profile();
            profile.setFullName(fullName);
            profile.setDepartment(department);
            profile.setProfileType(ProfileType.valueOf(profileType.toUpperCase()));

            if (file != null && !file.isEmpty()) {
                String contentType = file.getContentType();
                if ("image/jpeg".equals(contentType) || "image/png".equals(contentType)) {
                    File uploadFolder = new File(UPLOAD_DIR);
                    if (!uploadFolder.exists()) {
                        uploadFolder.mkdirs();
                    }

                    String cleanFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Path saveDestinationPath = Paths.get(UPLOAD_DIR + cleanFileName);
                    Files.write(saveDestinationPath, file.getBytes());

                    profile.setPhotoFileName(cleanFileName);
                    profile.setPhotoContentType(contentType);
                }
            }

            profileService.saveProfile(profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/"; // Now safely handles the redirection string
    }

    @PostMapping("/batch")
    @ResponseBody
    public ResponseEntity<List<Profile>> createBatchProfiles(@RequestBody List<Profile> profiles) {
        return new ResponseEntity<>(profileService.saveBatchProfiles(profiles), HttpStatus.CREATED);
    }

    // Browser Form Redirect Action for Update
    @PostMapping("/update")
    public String updateProfileFromForm(
            @RequestParam("id") Long id,
            @RequestParam("fullName") String fullName,
            @RequestParam("department") String department,
            @RequestParam("profileType") String profileType) {
        
        Profile existingProfile = profileService.getProfileById(id);
        if (existingProfile != null) {
            existingProfile.setFullName(fullName);
            existingProfile.setDepartment(department);
            existingProfile.setProfileType(ProfileType.valueOf(profileType.toUpperCase()));
            profileService.saveProfile(existingProfile);
        }
        return "redirect:/"; 
    }

    // Browser Form Redirect Action for Delete
    @GetMapping("/delete/{id}")
    public String deleteProfileFromForm(@PathVariable Long id) {
        if (profileService.getProfileById(id) != null) {
            profileService.deleteProfile(id);
        }
        return "redirect:/"; 
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        if (profileService.getProfileById(id) != null) {
            profileService.deleteProfile(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/qrcode")
    @ResponseBody
    public ResponseEntity<String> getProfileQRCode(@PathVariable Long id) {
        Profile profile = profileService.getProfileById(id);
        if (profile == null) return ResponseEntity.notFound().build();
        try {
            String verificationUrl = "https://ccun.edu.kh/verify/profile/" + profile.getUuid();
            String qrCodeBase64 = cardMediaService.generateQRCodeBase64(verificationUrl, 250, 250);
            return ResponseEntity.ok("data:image/png;base64," + qrCodeBase64);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating QR matrix");
        }
    }

    @GetMapping(value = "/{id}/export", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> downloadPdfCard(@PathVariable Long id) {
        Profile profile = profileService.getProfileById(id);
        if (profile == null) return ResponseEntity.notFound().build();
        
        byte[] pdfContents = pdfExportService.generateIdCardPdf(profile);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=ID_Card_" + profile.getRegistrationNumber() + ".pdf");
        
        return new ResponseEntity<>(pdfContents, headers, HttpStatus.OK);
    }
}

