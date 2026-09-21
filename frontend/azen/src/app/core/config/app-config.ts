export interface AppConfig {
  apiUrl: string;
}

declare global {
  interface Window {
    __AZEN_CONFIG__: AppConfig;
  }
}