import { Component, OnInit, signal } from "@angular/core";
import { RouterLink } from "@angular/router";
import { forkJoin } from "rxjs";
import { RecruitmentApiService } from "../core/recruitment-api.service";
import { SummaryCardComponent } from "../dashboard/summary-card.component";

@Component({
  selector: "app-admin-home",
  standalone: true,
  imports: [RouterLink, SummaryCardComponent],
  template: `
    <header class="page-heading">
      <div>
        <span class="eyebrow">Gestão</span>
        <h2>Resumo operacional</h2>
        <p>Acompanhe o volume de vagas e acesse os fluxos principais do RH.</p>
      </div>

      <a class="button-link" routerLink="/admin/vagas/nova">Cadastrar vaga</a>
    </header>

    @if (message()) {
      <p class="alert">{{ message() }}</p>
    }

    <section class="summary-grid">
      <app-summary-card label="Vagas abertas" [value]="openJobs()" />
      <app-summary-card label="Vagas fechadas" [value]="closedJobs()" />
      <app-summary-card label="Canceladas" [value]="cancelledJobs()" />
    </section>

    <section class="admin-actions-grid">
      <a class="compact-card admin-action-card" routerLink="/admin/vagas">
        <strong>Gerenciar vagas</strong>
        <span>Busque, filtre, edite ou cancele vagas cadastradas.</span>
      </a>

      <a class="compact-card admin-action-card" routerLink="/admin/candidaturas">
        <strong>Avaliar candidaturas</strong>
        <span>Veja candidatos por vaga, atualize status e registre avaliações.</span>
      </a>
    </section>
  `,
})
export class AdminHomeComponent implements OnInit {
  readonly openJobs = signal(0);
  readonly closedJobs = signal(0);
  readonly cancelledJobs = signal(0);
  readonly message = signal("");

  constructor(private readonly api: RecruitmentApiService) {}

  ngOnInit(): void {
    const filters = { query: "", department: "", location: "" };

    forkJoin({
      open: this.api.listJobs(filters, "OPEN", 0, 1),
      closed: this.api.listJobs(filters, "CLOSED", 0, 1),
      cancelled: this.api.listJobs(filters, "CANCELLED", 0, 1),
    }).subscribe({
      next: ({ open, closed, cancelled }) => {
        this.openJobs.set(open.totalElements);
        this.closedJobs.set(closed.totalElements);
        this.cancelledJobs.set(cancelled.totalElements);
      },
      error: () => {
        this.message.set("Não foi possível carregar o resumo da gestão.");
      },
    });
  }
}
