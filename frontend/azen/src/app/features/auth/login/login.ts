import { Component, inject } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { AuthStore } from '../../../core/auth/auth.store';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  readonly loginForm = this.fb.nonNullable.group({
    email: [
      '',
      [
        Validators.required,
        Validators.email
      ]
    ],

    password: [
      '',
      [
        Validators.required,
        Validators.minLength(8)
      ]
    ]
  });

  isSubmitting = false;
  errorMessage = '';

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    const credentials = this.loginForm.getRawValue();

    this.authService.login(credentials).subscribe({
          next: (response) => {
      console.log('Login successful');

      this.authStore.setTokens(
        response.accessToken,
        response.refreshToken
      );

      this.isSubmitting = false;

      this.router.navigate(['/dashboard']);
    },

      error: (error) => {
        console.error('Login error:', error);

        this.isSubmitting = false;

        if (error.status === 401) {
          this.errorMessage =
            'Email o contraseña incorrectos.';
        } else if (error.status === 0) {
          this.errorMessage =
            'No se pudo conectar con el servidor.';
        } else {
          this.errorMessage =
            'Ocurrió un error al iniciar sesión.';
        }
      }
    });
  }
}