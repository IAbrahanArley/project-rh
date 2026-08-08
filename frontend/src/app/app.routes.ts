import { Routes } from "@angular/router";
import { CandidateApplicationsPageComponent } from "./candidate/candidate-applications-page.component";
import { CandidateHomeComponent } from "./candidate/candidate-home.component";
import { CandidateJobsPageComponent } from "./candidate/candidate-jobs-page.component";
import { CandidateShellComponent } from "./candidate/candidate-shell.component";
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
    path: "admin",
    component: DashboardComponent,
    canActivate: [authGuard],
  },
  {
    path: "",
    component: CandidateShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: "",
        component: CandidateHomeComponent,
      },
      {
        path: "vagas",
        component: CandidateJobsPageComponent,
      },
      {
        path: "candidaturas",
        component: CandidateApplicationsPageComponent,
      },
    ],
  },
  {
    path: "**",
    redirectTo: "",
  },
];
