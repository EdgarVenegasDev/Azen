import { Component, inject } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly registerForm = this.fb.nonNullable.group(
    {
      firstName: [
        '',
        [
          Validators.required,
          Validators.minLength(2)
        ]
      ],

      lastName: [
        '',
        [
          Validators.required,
          Validators.minLength(2)
        ]
      ],

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
      ],

      confirmPassword: [
        '',
        [
          Validators.required
        ]
      ]
    },
    {
      validators: [this.passwordsMatch]
    }
  );

  isSubmitting = false;
  errorMessage = '';

  private passwordsMatch(
    control: AbstractControl
  ): ValidationErrors | null {

    const password = control.get('password')?.value;
    const confirmPassword =
      control.get('confirmPassword')?.value;

    if (password !== confirmPassword) {
      return {
        passwordMismatch: true
      };
    }

    return null;
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    const {
      confirmPassword,
      ...registerData
    } = this.registerForm.getRawValue();

    this.authService.register(registerData).subscribe({
      next: () => {
        console.log('Registration successful');

        this.isSubmitting = false;

        this.router.navigate(['/login']);
      },

      error: (error) => {
        console.error('Registration error:', error);

        this.isSubmitting = false;

        if (error.status === 409) {
          this.errorMessage =
            'El correo electrónico ya está registrado.';
        } else if (error.status === 400) {
          this.errorMessage =
            'Los datos proporcionados no son válidos.';
        } else if (error.status === 0) {
          this.errorMessage =
            'No se pudo conectar con el servidor.';
        } else {
          this.errorMessage =
            'Ocurrió un error al crear la cuenta.';
        }
      }
    });
  }
}
