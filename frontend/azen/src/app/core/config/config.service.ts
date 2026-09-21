import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  get apiUrl(): string {
    return window.__AZEN_CONFIG__.apiUrl;
  }
}