package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.library.service.MailService;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    // ✅ Send generic email (used for approval/rejection)
    @PostMapping
    public String sendMail(@RequestBody MailRequest request) {
        try {
            mailService.sendEmail(request.getEmail(), "Library Notification", request.getMessage());
            return "✅ Mail sent successfully to " + request.getEmail();
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Failed to send mail: " + e.getMessage();
        }
    }

    // ✅ Manual due-date reminder mail (used by librarian)
    @PostMapping("/reminder")
    public String sendReminder(@RequestBody MailRequest request) {
        try {
            mailService.sendEmail(request.getEmail(), "Library Reminder", request.getMessage());
            return "📧 Reminder mail sent manually to " + request.getEmail();
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Failed to send reminder: " + e.getMessage();
        }
    }

    // ✅ Simple DTO class inside controller
    static class MailRequest {
        private String email;
        private String message;

        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }

        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
