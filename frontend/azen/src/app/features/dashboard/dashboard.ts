import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

import { AuthStore } from '../../core/auth/auth.store';

@Component({
  selector: 'app-dashboard',
  imports: [],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard {
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  logout(): void {
    this.authStore.clearSession();
    this.router.navigate(['/login']);
  }
}