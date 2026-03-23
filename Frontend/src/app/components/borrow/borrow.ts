import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-borrow',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './borrow.html',
  styleUrls: ['./borrow.css']
})
export default class Borrow implements OnInit {
  borrowedBooks: any[] = [];
  userEmail: string = '';
  loading: boolean = false;
  errorMessage: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    const user = sessionStorage.getItem('user');
    if (user) {
      this.userEmail = JSON.parse(user).email;
      this.loadBorrowedBooks();
    } else {
      this.errorMessage = 'User not found in session. Please log in again.';
    }
  }

 loadBorrowedBooks() {
  const encodedEmail = encodeURIComponent(this.userEmail);
  this.http.get<any[]>(`http://localhost:8080/api/borrow/user/email/${encodedEmail}`)
    .subscribe({
      next: data => this.borrowedBooks = data,
      error: err => {
        console.error('Error fetching borrowed books:', err);
        alert('Failed to load borrowed books.');
      }
    });
}


  returnBook(recordId: number) {
    if (!recordId) {
      alert('Invalid record ID.');
      return;
    }

    if (confirm('Are you sure you want to return this book?')) {
      this.http.post(`http://localhost:8080/api/borrow/return/${recordId}`, {}, { responseType: 'text' })
        .subscribe({
          next: (response) => {
            alert(response || 'Book returned successfully!');
            this.loadBorrowedBooks();
          },
          error: () => {
            alert('Failed to return the book. Please try again.');
          }
        });
    }
  }
}
