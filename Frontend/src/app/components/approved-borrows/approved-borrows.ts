import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';

@Component({
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './approved-borrows.html'
})
export default class ApprovedBorrows implements OnInit, OnDestroy {

  approvedRecords: any[] = [];
  sentReminders: Set<number> = new Set();
  intervalId: any;

  private borrowApi = 'http://localhost:8080/api/borrow';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadApprovedRecords();

    // 🔁 auto refresh every 5 seconds
    this.intervalId = setInterval(() => {
      this.loadApprovedRecords();
    }, 5000);
  }

  ngOnDestroy() {
    clearInterval(this.intervalId);
  }

  loadApprovedRecords() {
    this.http.get<any[]>(`${this.borrowApi}/approved`).subscribe({
      next: (data) => this.approvedRecords = data
    });
  }

  sendManualReminder(recordId: number) {
    if (this.sentReminders.has(recordId)) {
      alert('⚠️ Reminder already sent recently.');
      return;
    }

    this.http.post(
      `${this.borrowApi}/reminder/${recordId}`,
      {},
      { responseType: 'text' }
    ).subscribe({
      next: (msg) => {
        alert(msg);
        this.sentReminders.add(recordId);
      },
      error: () => alert('❌ Failed to send reminder')
    });
  }
}
