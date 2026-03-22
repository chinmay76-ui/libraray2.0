package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.repository.UserRepository;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
import com.library.service.BorrowService;
import com.library.service.UserService;
import com.library.service.BookService;
import com.library.service.MailService;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    // =========================
    // DTO for frontend request
    // =========================
    public static class BorrowRequestData {
        private String userEmail;
        private Long bookId;
        private String librarianEmail;

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }

        public String getLibrarianEmail() { return librarianEmail; }
        public void setLibrarianEmail(String librarianEmail) { this.librarianEmail = librarianEmail; }
    }

    // ==================================================
    // 1. Borrow request from frontend (student dashboard)
    // ==================================================
    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestBorrowFrontend(@RequestBody BorrowRequestData data) {
        // user by email (Optional<User>)
        User user = userRepository.findByEmail(data.getUserEmail())
                .orElse(null);
        // book by id
        Book book = bookRepository.findById(data.getBookId()).orElse(null);
        // optional librarian
        User librarian = (data.getLibrarianEmail() != null)
                ? userRepository.findByEmail(data.getLibrarianEmail()).orElse(null)
                : null;

        if (user == null || book == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "❌ Invalid user or book details."));
        }

        // check active or pending borrow
        List<BorrowRecord> activeRecords = borrowRepository.findByUser(user);
        boolean hasActiveBorrow = activeRecords.stream()
                .anyMatch(r -> r.getStatus().equalsIgnoreCase("Borrowed")
                        || r.getStatus().equalsIgnoreCase("Pending"));

        if (hasActiveBorrow) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "message",
                            "⚠️ You already have an active borrowed or pending book. Please return it first."
                    ));
        }

        if (book.getAvailableCopies() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "❌ Book not available for borrowing."));
        }

        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(14));
        record.setStatus("Pending");
        record.setReturned(false);

        if (librarian != null) {
            record.setApprovedBy(librarian);
        }

        borrowRepository.save(record);

        // notify librarian
        String targetEmail = (librarian != null) ? librarian.getEmail() : "librarian@example.com";
        mailService.sendEmail(
                targetEmail,
                "New Borrow Request",
                "A new borrow request has been submitted by "
                        + user.getName()
                        + " for the book \"" + book.getTitle() + "\"."
        );

        return ResponseEntity.ok(Map.of(
                "message",
                "✅ Borrow request submitted for \"" + book.getTitle() + "\". Awaiting librarian approval."
        ));
    }

    // ==========================================
    // 2. Optional: request by userId + bookId
    // ==========================================
    @PostMapping("/{userId}/{bookId}")
    public String requestBorrow(@PathVariable Long userId, @PathVariable Long bookId) {
        User user = userService.getUserById(userId);
        Book book = bookService.getBookById(bookId);

        if (user == null || book == null) {
            return "❌ Invalid user or book ID.";
        }

        if (book.getAvailableCopies() <= 0) {
            return "❌ Book not available for borrowing.";
        }

        borrowService.requestBorrow(user, book);
        return "✅ Borrow request submitted for \"" + book.getTitle() + "\". Awaiting librarian approval.";
    }

    // ============================
    // 3. Return a borrowed book
    // ============================
    @PostMapping("/return/{recordId}")
    public String returnBook(@PathVariable Long recordId) {
        BorrowRecord record = borrowService.returnBook(recordId);
        if (record != null) {
            return "📚 " + record.getBook().getTitle() + " returned successfully!";
        } else {
            return "❌ Invalid record or already returned.";
        }
    }

    // ===========================================
    // 4. Borrowed books for a user (by userId)
    // ===========================================
    @GetMapping("/user/{userId}")
    public List<BorrowRecord> getUserBorrowedBooks(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return borrowService.getBorrowedBooks(user);
    }

    // ======================================================
    // 5. Borrowed books for a user (by email, student page)
    // ======================================================
    @GetMapping("/user/email/{encodedEmail}")
    public ResponseEntity<?> getBorrowedBooksByUserEmail(@PathVariable String encodedEmail) {
        try {
            String email = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8);
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found for email: " + email));
            }

            List<BorrowRecord> borrowedBooks = borrowService.getBorrowedBooks(user);
            return ResponseEntity.ok(borrowedBooks);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Error fetching borrowed books: " + e.getMessage()));
        }
    }

    // ============================================
    // 6. Pending and approved lists for librarian
    // ============================================
    @GetMapping("/pending")
    public ResponseEntity<List<BorrowRecord>> getPendingRequests() {
        List<BorrowRecord> pendingRecords = borrowRepository.findByStatusIgnoreCase("Pending");
        return ResponseEntity.ok(pendingRecords);
    }

    @GetMapping("/approved")
    public List<BorrowRecord> getApprovedBorrowRecords() {
        return borrowRepository.findByStatusIgnoreCase("Borrowed");
    }

    // ============================================
    // 7. Approve / reject / reminder
    // ============================================
    @PostMapping("/approve/{recordId}/{librarianId}")
    public String approveBorrowRequest(@PathVariable Long recordId, @PathVariable Long librarianId) {
        User librarian = userService.getUserById(librarianId);
        borrowService.approveBorrow(recordId, librarian);
        return "✅ Borrow request approved successfully!";
    }

    @PostMapping("/reject/{recordId}/{librarianId}")
    public String rejectBorrowRequest(@PathVariable Long recordId, @PathVariable Long librarianId) {
        User librarian = userService.getUserById(librarianId);
        borrowService.rejectBorrow(recordId, librarian);
        return "❌ Borrow request rejected.";
    }

    @PostMapping("/reminder/{recordId}")
    public String sendReminder(@PathVariable Long recordId) {
        borrowService.sendReminder(recordId);
        return "✅ Reminder email sent successfully!";
    }
}
