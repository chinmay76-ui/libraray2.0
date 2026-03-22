package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.library.entity.Book;
import com.library.service.BookService;

@RestController
@RequestMapping("/api/librarian")
public class LibrarianController {

    @Autowired
    private BookService bookService;

    // ✅ Add a new book
    @PostMapping("/books")
    public String addBook(@RequestBody Book book) {
        bookService.saveBook(book);
        return "✅ Book added successfully: " + book.getTitle();
    }

    // ✅ Update an existing book
    @PutMapping("/books/{id}")
    public String updateBook(@PathVariable Long id, @RequestBody Book updatedBook) {
        Book existingBook = bookService.getBookById(id);
        if (existingBook == null) {
            return "❌ Book not found with ID: " + id;
        }

        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setAvailableCopies(updatedBook.getAvailableCopies());
        existingBook.setTotalCopies(updatedBook.getTotalCopies());

        bookService.saveBook(existingBook);
        return "✅ Book updated successfully: " + existingBook.getTitle();
    }

    // ✅ Delete a book
    @DeleteMapping("/books/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "🗑️ Book deleted successfully!";
    }

    // ✅ View all books
    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }
}
