package com.devops.idcard.controller;

import com.devops.idcard.model.Profile;
import com.devops.idcard.service.CardMediaService;
import com.devops.idcard.service.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {

    private final ProfileService profileService;
    private final CardMediaService cardMediaService;

    public WebController(ProfileService profileService, CardMediaService cardMediaService) {
        this.profileService = profileService;
        this.cardMediaService = cardMediaService;
    }

    // This handles loading the dashboard on http://localhost:8080/
    @GetMapping("/")
    public String indexPage(Model model) {
        model.addAttribute("profiles", profileService.getAllProfiles());
        return "index"; 
    }

    // This handles loading the ID Card layout on http://localhost:8080/preview/{id}
    @GetMapping("/preview/{id}")
    public String previewCard(@PathVariable Long id, Model model) {
        Profile profile = profileService.getProfileById(id);
        if (profile == null) return "redirect:/";

        try {
            String verificationUrl = "https://ccun.edu.kh/verify/profile/" + profile.getUuid();
            String qrCode = "data:image/png;base64," + cardMediaService.generateQRCodeBase64(verificationUrl, 150, 150);
            String barcode = "data:image/png;base64," + cardMediaService.generateBarcodeBase64(profile.getRegistrationNumber(), profile.getBarcodeType(), 200, 50);

            model.addAttribute("profile", profile);
            model.addAttribute("qrCode", qrCode);
            model.addAttribute("barcode", barcode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "card-template"; 
    }
}

