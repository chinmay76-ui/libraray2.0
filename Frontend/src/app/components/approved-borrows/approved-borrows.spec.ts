import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApprovedBorrows } from './approved-borrows';

describe('ApprovedBorrows', () => {
  let component: ApprovedBorrows;
  let fixture: ComponentFixture<ApprovedBorrows>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApprovedBorrows]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ApprovedBorrows);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
