import { Component, OnInit, signal } from "@angular/core";
import { Router, RouterLink } from "@angular/router";
import { ApplicationListComponent } from "../applications/application-list.component";
import { AuthService } from "../core/auth.service";
import { JobApplication } from "../core/models";
import { RecruitmentApiService } from "../core/recruitment-api.service";

@Component({
  selector: "app-candidate-applications-page",
  standalone: true,
  imports: [ApplicationListComponent, RouterLink],
  template: `
    <header class="page-heading">
      <div>
        <span class="eyebrow">Candidaturas</span>
        <h2>Acompanhe seus processos</h2>
        <p>Veja o status das vagas em que você já se candidatou.</p>
      </div>

      <a class="button-link" routerLink="/vagas">Buscar vagas</a>
    </header>

    @if (message()) {
      <p class="alert">{{ message() }}</p>
    }

    @if (loading()) {
      <p class="feedback">Carregando candidaturas...</p>
    }

    <app-application-list [applications]="applications()" />
  `,
})
export class CandidateApplicationsPageComponent implements OnInit {
  readonly applications = signal<JobApplication[]>([]);
  readonly loading = signal(false);
  readonly message = signal("");

  constructor(
    private readonly authService: AuthService,
    private readonly api: RecruitmentApiService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    const session = this.authService.session();

    if (!session) {
      void this.router.navigateByUrl("/login");
      return;
    }

    if (session.user.role === "ADMIN") {
      void this.router.navigateByUrl("/admin");
      return;
    }

    this.loading.set(true);
    this.api.listMyApplications(session.token).subscribe({
      next: (applications) => {
        this.applications.set(applications.content);
        this.loading.set(false);
      },
      error: () => {
        this.message.set("Não foi possível carregar suas candidaturas.");
        this.loading.set(false);
      },
    });
  }
}
