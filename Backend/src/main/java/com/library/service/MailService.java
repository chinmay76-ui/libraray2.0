package com.library.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
public class MailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Value("${BREVO_SENDER_EMAIL}")
    private String senderEmail;

    @Value("${BREVO_SENDER_NAME}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ Generic email sender (Brevo)
    public void sendEmail(String to, String subject, String body) {

        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        // 🔐 JSON-safe content
        String safeBody = body
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "<br>");

        String safeSubject = subject.replace("\"", "\\\"");

        String payload = """
        {
          "sender": {
            "name": "%s",
            "email": "%s"
          },
          "to": [
            { "email": "%s" }
          ],
          "subject": "%s",
          "htmlContent": "%s"
        }
        """.formatted(
                senderName,
                senderEmail,
                to,
                safeSubject,
                safeBody
        );

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(url, entity, String.class);
    }

  

    // ✅ Registration mail
    public void sendRegistrationMail(String to, String username) {
        String subject = "Welcome to Library Management System";
        String body = "Hello " + username + ",<br><br>"
                + "🎉 Your Library Management System account has been created successfully!<br>"
                + "You can now log in and start borrowing books.<br><br>"
                + "Best Regards,<br>Library Team 📚";
        sendEmail(to, subject, body);
    }

    // ✅ Borrow mail (Approved)
    public void sendBorrowMail(String to, String username, String bookTitle, String dueDate) {
        String subject = "✅ Borrow Request Approved";
        String body = "Hello " + username + ",<br><br>"
                + "Your borrow request for the book <b>\"" + bookTitle + "\"</b> has been approved.<br>"
                + "📅 Due Date: <b>" + dueDate + "</b><br><br>"
                + "Please collect and return the book on time to avoid penalties.<br><br>"
                + "Happy Reading!<br>Library Team 📚";
        sendEmail(to, subject, body);
    }

    // ✅ Rejection mail
    public void sendRejectionMail(String to, String username, String bookTitle) {
        String subject = "❌ Borrow Request Rejected";
        String body = "Hello " + username + ",<br><br>"
                + "Your borrow request for the book <b>\"" + bookTitle + "\"</b> has been rejected by the librarian.<br><br>"
                + "You may try borrowing a different book or contact the library for more details.<br><br>"
                + "Regards,<br>Library Team 📚";
        sendEmail(to, subject, body);
    }

    // ✅ Return mail
    public void sendReturnMail(String to, String username, String bookTitle) {
        String subject = "📘 Book Returned Successfully";
        String body = "Hello " + username + ",<br><br>"
                + "Thank you for returning the book <b>\"" + bookTitle + "\"</b>.<br>"
                + "We hope you enjoyed reading it!<br><br>"
                + "Regards,<br>Library Team 📚";
        sendEmail(to, subject, body);
    }

    // ✅ Due / Overdue reminder
    public void sendDueReminderMail(String to, String username, String bookTitle, String dueDate, boolean overdue) {

        String subject;
        String body;

        if (overdue) {
            subject = "⏰ Overdue Book Reminder";
            body = "Hello " + username + ",<br><br>"
                    + "The book <b>\"" + bookTitle + "\"</b> was due on <b>" + dueDate + "</b>.<br>"
                    + "Please return it as soon as possible to avoid penalties.<br><br>"
                    + "Regards,<br>Library Team 📚";
        } else {
            subject = "📅 Upcoming Due Date Reminder";
            body = "Hello " + username + ",<br><br>"
                    + "Reminder: The book <b>\"" + bookTitle + "\"</b> is due on <b>" + dueDate + "</b>.<br>"
                    + "Please return it on or before the due date.<br><br>"
                    + "Happy Reading!<br>Library Team 📚";
        }

        sendEmail(to, subject, body);
    }
}
