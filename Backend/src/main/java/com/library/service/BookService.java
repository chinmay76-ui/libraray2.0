package com.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import com.library.entity.Book;
import com.library.repository.BookRepository;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    // ✅ Fetch all books
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // ✅ Get book by ID
    public Book getBookById(Long id) {
        Optional<Book> book = bookRepository.findById(id);
        return book.orElse(null);
    }

    // ✅ Add or update a book
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    // ✅ Delete a book
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
 // ✅ Decrease available copies
    public void decreaseAvailableCopies(Book book) {
        if (book.getAvailableCopies() > 0) {
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            bookRepository.save(book);
        }
    }

    // ✅ Increase available copies
    public void increaseAvailableCopies(Book book) {
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
    }

}
