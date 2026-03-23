import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Borrow } from './borrow';

describe('Borrow', () => {
  let component: Borrow;
  let fixture: ComponentFixture<Borrow>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Borrow]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Borrow);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
