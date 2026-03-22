package com.library.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.repository.BorrowRepository;

@Service
public class BorrowService {

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private MailService mailService;

    // ✅ Step 1: Student requests to borrow
    public BorrowRecord requestBorrow(User user, Book book) {
        if (book == null || user == null) throw new IllegalArgumentException("User or Book cannot be null.");
        if (book.getAvailableCopies() <= 0) throw new IllegalStateException("No available copies of this book.");

        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(14)); // ✅ Auto 2-week due date
        record.setStatus("PENDING"); // unified case
        record.setReturned(false);
        borrowRepository.save(record);

        return record;
    }

    // ✅ Step 2: Librarian approves borrow request
    public BorrowRecord approveBorrow(Long recordId, User librarian) {
        BorrowRecord record = borrowRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid borrow record ID."));
        if (!"PENDING".equalsIgnoreCase(record.getStatus()))
            throw new IllegalStateException("Request already processed.");

        record.setStatus("BORROWED");
        record.setApprovedBy(librarian);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(14)); // reset due date on approval
        bookService.decreaseAvailableCopies(record.getBook());
        borrowRepository.save(record);

        // Notify student
        mailService.sendBorrowMail(
                record.getUser().getEmail(),
                record.getUser().getName(),
                record.getBook().getTitle(),
                record.getDueDate().toString()
        );

        return record;
    }

    // ✅ Step 3: Librarian rejects borrow request
    public BorrowRecord rejectBorrow(Long recordId, User librarian) {
        BorrowRecord record = borrowRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid borrow record ID."));
        if (!"PENDING".equalsIgnoreCase(record.getStatus()))
            throw new IllegalStateException("Request already processed.");

        record.setStatus("REJECTED");
        record.setApprovedBy(librarian);
        borrowRepository.save(record);

        // Notify student
        mailService.sendRejectionMail(
                record.getUser().getEmail(),
                record.getUser().getName(),
                record.getBook().getTitle()
        );

        return record;
    }

    // ✅ Step 4: Student returns a book
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = borrowRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow record not found."));
        if (record.getReturnDate() != null)
            throw new IllegalStateException("Book already returned.");

        record.setReturnDate(LocalDate.now());
        record.setStatus("RETURNED");
        record.setReturned(true);
        bookService.increaseAvailableCopies(record.getBook());
        borrowRepository.save(record);

        mailService.sendReturnMail(
                record.getUser().getEmail(),
                record.getUser().getName(),
                record.getBook().getTitle()
        );

        return record;
    }

    // ✅ Step 5: Get all borrowed books for a student
    public List<BorrowRecord> getBorrowedBooks(User user) {
        List<BorrowRecord> records = borrowRepository.findByUser(user);
        updateOverdueStatuses(records);
        return records;
    }

    // ✅ Step 6: Get all borrow records (used by librarian dashboard)
    public List<BorrowRecord> getAllBorrowRecords() {
        List<BorrowRecord> records = borrowRepository.findAll();
        updateOverdueStatuses(records);
        return records;
    }

    // ✅ Step 7: Send reminder (manual or scheduled)
    public void sendManualReminder(Long recordId, boolean overdue) {
        BorrowRecord record = borrowRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid record ID"));

        mailService.sendDueReminderMail(
                record.getUser().getEmail(),
                record.getUser().getName(),
                record.getBook().getTitle(),
                record.getDueDate().toString(),
                overdue
        );
    }

    public void sendReminder(Long recordId) {
        sendManualReminder(recordId, false);
    }

    // ✅ Helper: mark overdue books automatically
    private void updateOverdueStatuses(List<BorrowRecord> records) {
        for (BorrowRecord r : records) {
            if (r.getReturnDate() == null && r.getDueDate() != null && LocalDate.now().isAfter(r.getDueDate())) {
                r.setStatus("OVERDUE");
                borrowRepository.save(r);
            }
        }
    }
}
