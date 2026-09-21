import { inject } from '@angular/core';
import {
  HttpInterceptorFn
} from '@angular/common/http';

import { AuthStore } from '../auth/auth.store';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authStore = inject(AuthStore);

  const token = authStore.getAccessToken();

  const isAuthEndpoint =
    req.url.includes('/api/v1/auth/login') ||
    req.url.includes('/api/v1/auth/register') ||
    req.url.includes('/api/v1/auth/refresh-token');

  if (!token || isAuthEndpoint) {
    return next(req);
  }

  const authenticatedRequest = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(authenticatedRequest);
};