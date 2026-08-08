import { Routes } from "@angular/router";
import { AdminApplicationsPageComponent } from "./admin/admin-applications-page.component";
import { AdminHomeComponent } from "./admin/admin-home.component";
import { AdminJobFormPageComponent } from "./admin/admin-job-form-page.component";
import { AdminJobsPageComponent } from "./admin/admin-jobs-page.component";
import { AdminShellComponent } from "./admin/admin-shell.component";
import { CandidateApplicationsPageComponent } from "./candidate/candidate-applications-page.component";
import { CandidateHomeComponent } from "./candidate/candidate-home.component";
import { CandidateJobsPageComponent } from "./candidate/candidate-jobs-page.component";
import { CandidateShellComponent } from "./candidate/candidate-shell.component";
import { adminGuard, candidateGuard, guestGuard } from "./core/auth.guard";
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
    component: AdminShellComponent,
    canActivate: [adminGuard],
    children: [
      {
        path: "",
        component: AdminHomeComponent,
      },
      {
        path: "vagas",
        component: AdminJobsPageComponent,
      },
      {
        path: "vagas/nova",
        component: AdminJobFormPageComponent,
      },
      {
        path: "vagas/:id/editar",
        component: AdminJobFormPageComponent,
      },
      {
        path: "candidaturas",
        component: AdminApplicationsPageComponent,
      },
    ],
  },
  {
    path: "",
    component: CandidateShellComponent,
    canActivate: [candidateGuard],
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
