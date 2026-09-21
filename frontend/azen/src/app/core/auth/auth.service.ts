import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs';

import {
  AuthResponse,
  LoginRequest,
  RegisterRequest
} from './auth.types';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8081/api/v1/auth';

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.apiUrl}/login`,
      credentials
    );
  }

  register(data: RegisterRequest): Observable<void> {
    return this.http.post(
        `${this.apiUrl}/register`,
        data,
        {
        responseType: 'text'
        }
    ).pipe(
        map(() => void 0)
    );
    }

  refreshToken(
    refreshToken: string
  ): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.apiUrl}/refresh-token`,
      { refreshToken }
    );
  }
}