import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-admin-summary',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-summary.html',
  styleUrls: ['./admin-summary.css']
})
export default class AdminSummary {
  users: any[] = [];
  summary: string = '';
  permanentAdminEmail = 'admin@gmail.com';

  totalStudents: number = 0;
  totalAdmins: number = 0;
  totalLibrarians: number = 0;

  constructor(private http: HttpClient) {
    this.loadUsers();
  }

  loadUsers() {
    this.http.get<any[]>('http://localhost:8080/api/admin/users').subscribe({
      next: (data) => {
        this.users = data;
        this.calculateTotals();
      },
      error: (err) => {
        console.error('❌ Failed to load users:', err);
      }
    });
  }

  calculateTotals() {
    this.totalStudents = this.users.filter(user => user.role === 'STUDENT').length;
    this.totalAdmins = this.users.filter(user => user.role === 'ADMIN').length;
    this.totalLibrarians = this.users.filter(user => user.role === 'LIBRARIAN').length;
  }

  assignRole(id: number, role: string) {
    this.http.put(
      `http://localhost:8080/api/admin/users/${id}/role?newRole=${role}`,
      {},
      { responseType: 'text' }
    ).subscribe({
      next: (msg) => {
        alert(msg);
        this.loadUsers();
      },
      error: () => {
        alert('❌ Failed to update role');
      }
    });
  }

  deleteUser(id: number, email: string) {
    if (email === this.permanentAdminEmail) {
      alert('⛔ Cannot delete permanent admin!');
      return;
    }
    if (confirm(`Are you sure you want to delete ${email}?`)) {
      this.http.delete(`http://localhost:8080/api/admin/users/${id}`, { responseType: 'text' }).subscribe({
        next: (msg) => {
          alert(msg);
          this.loadUsers();
        },
        error: () => {
          alert('❌ Failed to delete user');
        }
      });
    }
  }
}
