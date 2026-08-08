import { Routes } from "@angular/router";
import { authGuard, guestGuard } from "./core/auth.guard";
import { DashboardComponent } from "./dashboard/dashboard.component";
import { LoginComponent } from "./login/login.component";
import { RegisterComponent } from "./register/register.component";

export const routes: Routes = [
  {
    path: "login",
    component: LoginComponent,
    canActivate: [guestGuard],
  },
  {
    path: "register",
    component: RegisterComponent,
    canActivate: [guestGuard],
  },
  {
    path: "",
    component: DashboardComponent,
    canActivate: [authGuard],
  },
  {
    path: "**",
    redirectTo: "",
  },
];
