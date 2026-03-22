package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import com.library.entity.Book;
import com.library.repository.BookRepository;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    // Get all books
    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Add new book (JSON only – existing flow)
    @PostMapping
    public Book addBook(@RequestBody Book book) {
        return bookRepository.save(book);
    }

    // NEW: Add book with image (FormData from Angular)
    @PostMapping("/upload")
    public Book addBookWithImage(
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("totalCopies") int totalCopies,
            @RequestParam("availableCopies") int availableCopies,
            @RequestParam("image") MultipartFile image
    ) throws IOException {

        // 1) Save file to local folder "uploads"
        String uploadDir = "uploads";
        Files.createDirectories(Path.of(uploadDir));

        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        Path filePath = Path.of(uploadDir, fileName);
        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 2) Create Book and set fields
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(availableCopies);

        // 3) Set imageUrl so Angular can display it
        String imageUrl = "https://backend-production-35363.up.railway.app/images/" + fileName;
        book.setImageUrl(imageUrl);

        return bookRepository.save(book);
    }

    // Get book by ID
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    // Update book
    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book bookDetails) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            book.setTitle(bookDetails.getTitle());
            book.setAuthor(bookDetails.getAuthor());
            book.setIsbn(bookDetails.getIsbn());
            book.setCategory(bookDetails.getCategory());
            book.setTotalCopies(bookDetails.getTotalCopies());
            book.setAvailableCopies(bookDetails.getAvailableCopies());
            // keep existing imageUrl; do not overwrite unless provided
            if (bookDetails.getImageUrl() != null) {
                book.setImageUrl(bookDetails.getImageUrl());
            }
            return bookRepository.save(book);
        }
        return null;
    }

    // Delete book
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookRepository.deleteById(id);
    }
}
