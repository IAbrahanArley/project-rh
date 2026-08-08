import { environment } from "../../environments/environment";

interface RuntimeConfig {
  apiBaseUrl?: string;
}

declare global {
  interface Window {
    RH_RUNTIME_CONFIG?: RuntimeConfig;
  }
}

export const appConfig = {
  apiBaseUrl: window.RH_RUNTIME_CONFIG?.apiBaseUrl ?? environment.apiBaseUrl,
};
