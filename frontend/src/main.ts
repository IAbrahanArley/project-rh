import "zone.js";
import { bootstrapApplication } from "@angular/platform-browser";
import { provideHttpClient, withInterceptors } from "@angular/common/http";
import { provideRouter } from "@angular/router";
import { AppComponent } from "./app/app.component";
import { routes } from "./app/app.routes";
import { authErrorInterceptor } from "./app/core/auth-error.interceptor";

bootstrapApplication(AppComponent, {
  providers: [provideHttpClient(withInterceptors([authErrorInterceptor])), provideRouter(routes)],
});
