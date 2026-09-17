import { Component, inject } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly registerForm = this.fb.nonNullable.group(
    {
      firstName: ['', [
        Validators.required,
        Validators.minLength(2)
      ]],

      lastName: ['', [
        Validators.required,
        Validators.minLength(2)
      ]],

      email: ['', [
        Validators.required,
        Validators.email
      ]],

      password: ['', [
        Validators.required,
        Validators.minLength(8)
      ]],

      confirmPassword: ['', [
        Validators.required
      ]]
    },
    {
      validators: [this.passwordsMatch]
    }
  );

  isSubmitting = false;

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

    const {
      confirmPassword,
      ...registerData
    } = this.registerForm.getRawValue();

    console.log('Register data:', registerData);

    setTimeout(() => {
      this.isSubmitting = false;

      this.router.navigate(['/login']);
    }, 1000);
  }
}
