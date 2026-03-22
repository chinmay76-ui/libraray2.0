package com.library.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.library.entity.BorrowRecord;
import com.library.repository.BorrowRepository;
import com.library.service.MailService;

@RestController
@RequestMapping("/api/reminder")
public class ReminderController {

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private MailService mailService;

    // ✅ Manually trigger reminders
    @PostMapping("/send")
    public String sendDueReminders() {
        LocalDate today = LocalDate.now();
        List<BorrowRecord> borrowedRecords = borrowRepository.findByStatus("BORROWED");

        int reminderCount = 0;

        for (BorrowRecord record : borrowedRecords) {
            LocalDate dueDate = record.getDueDate();
            if (dueDate != null) {
                long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);

                if (daysUntilDue == 1) {
                    mailService.sendDueReminderMail(
                        record.getUser().getEmail(),
                        record.getUser().getName(),
                        record.getBook().getTitle(),
                        dueDate.toString(),
                        false
                    );
                    reminderCount++;
                } else if (daysUntilDue < 0) {
                    mailService.sendDueReminderMail(
                        record.getUser().getEmail(),
                        record.getUser().getName(),
                        record.getBook().getTitle(),
                        dueDate.toString(),
                        true
                    );
                    reminderCount++;
                }
            }
        }

        return "✅ " + reminderCount + " reminder(s) sent successfully!";
    }
}
