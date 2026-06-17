package com.devops.idcard.controller;

import com.devops.idcard.model.Profile;
import com.devops.idcard.model.ProfileType;
import com.devops.idcard.service.CardMediaService;
import com.devops.idcard.service.PdfExportService;
import com.devops.idcard.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;
    // Local directory folder name where images will be physically saved
    private static final String UPLOAD_DIR = "uploads/";

    public ProfileController(ProfileService profileService, CardMediaService cardMediaService, PdfExportService pdfExportService) {
        this.profileService = profileService;
        this.cardMediaService = cardMediaService;
        this.pdfExportService = pdfExportService;
    }

    @GetMapping
    public List<Profile> getAllProfiles() {
        return profileService.getAllProfiles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profile> getProfileById(@PathVariable Long id) {
        Profile profile = profileService.getProfileById(id);
        return profile != null ? ResponseEntity.ok(profile) : ResponseEntity.notFound().build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("department") String department,
            @RequestParam("profileType") String profileType,
            @RequestParam(value = "photo", required = false) MultipartFile file) {
        try {
            Profile profile = new Profile();
            profile.setFullName(fullName);
            profile.setDepartment(department);
            profile.setProfileType(ProfileType.valueOf(profileType.toUpperCase()));

            // Photo Management Layer
            if (file != null && !file.isEmpty()) {
                String contentType = file.getContentType();
                if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
                    return ResponseEntity.badRequest().body("Validation Error: Only JPEG and PNG images are supported.");
                }

                // Ensure physical upload directory path exists on disk
                File uploadFolder = new File(UPLOAD_DIR);
                if (!uploadFolder.exists()) {
                    uploadFolder.mkdirs();
                }

                // Construct an isolated unique filename string
                String cleanFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path saveDestinationPath = Paths.get(UPLOAD_DIR + cleanFileName);
                Files.write(saveDestinationPath, file.getBytes());

                // Bind attributes matching your teacher's schema properties
                profile.setPhotoFileName(cleanFileName);
                profile.setPhotoContentType(contentType);
            }

            Profile savedProfile = profileService.saveProfile(profile);
            return new ResponseEntity<>(savedProfile, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation Error: Invalid Profile Type conversion lookup.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving file to disk storage.");
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Profile>> createBatchProfiles(@RequestBody List<Profile> profiles) {
        return new ResponseEntity<>(profileService.saveBatchProfiles(profiles), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        if (profileService.getProfileById(id) != null) {
            profileService.deleteProfile(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Inject the new services via constructor update or @Autowired field properties
    private final com.devops.idcard.service.CardMediaService cardMediaService;

    private final com.devops.idcard.service.PdfExportService pdfExportService;

    // Get live Base64 QR code visualization for verification urls
    @GetMapping("/{id}/qrcode")
    public ResponseEntity<String> getProfileQRCode(@PathVariable Long id) {
        Profile profile = profileService.getProfileById(id);
        if (profile == null) return ResponseEntity.notFound().build();
        try {
            // Embeds verification URL payload inside QR code matrix
            String verificationUrl = "https://ccun.edu.kh/verify/profile/" + profile.getUuid();
            String qrCodeBase64 = cardMediaService.generateQRCodeBase64(verificationUrl, 250, 250);
            return ResponseEntity.ok("data:image/png;base64," + qrCodeBase64);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating QR matrix");
        }
    }

    // Download Generated PDF Card Layout 
    @GetMapping(value = "/{id}/export", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPdfCard(@PathVariable Long id) {
        Profile profile = profileService.getProfileById(id);
        if (profile == null) return ResponseEntity.notFound().build();
        
        byte[] pdfContents = pdfExportService.generateIdCardPdf(profile);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=ID_Card_" + profile.getRegistrationNumber() + ".pdf");
        
        return new ResponseEntity<>(pdfContents, headers, HttpStatus.OK);
    }
}

