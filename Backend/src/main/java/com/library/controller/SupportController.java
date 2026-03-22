package com.library.controller;

import com.library.dto.SupportRequest;
import com.library.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    @Autowired
    private MailService mailService;

    @PostMapping
    public ResponseEntity<?> sendSupportMail(@RequestBody SupportRequest request) {

        String subject = "📩 LMS Support Request";

        String body =
                "Name: " + request.getName() + "\n" +
                "Email: " + request.getEmail() + "\n\n" +
                "Message:\n" +
                request.getMessage();

        // ⚠️ MUST be Brevo-verified email
        mailService.sendEmail(
                "mymillam01@gmail.com",
                subject,
                body
        );

        return ResponseEntity.ok(
                java.util.Map.of("message", "Support message sent successfully")
        );

    }
}
