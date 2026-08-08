import { Component, OnInit, signal } from "@angular/core";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { AuthService } from "../core/auth.service";
import { JobOpening, JobOpeningRequest } from "../core/models";
import { RecruitmentApiService } from "../core/recruitment-api.service";
import { JobFormComponent } from "../jobs/job-form.component";

@Component({
  selector: "app-admin-job-form-page",
  standalone: true,
  imports: [JobFormComponent, RouterLink],
  template: `
    <header class="page-heading">
      <div>
        <span class="eyebrow">Vagas</span>
        <h2>{{ jobId() ? "Editar vaga" : "Cadastrar vaga" }}</h2>
        <p>{{ jobId() ? "Atualize os dados e o status da oportunidade." : "Crie uma nova oportunidade interna." }}</p>
      </div>

      <a class="button-link ghost-link" routerLink="/admin/vagas">Voltar para vagas</a>
    </header>

    @if (message()) {
      <p class="alert">{{ message() }}</p>
    }

    <section class="single-panel-page">
      <app-job-form
        [loading]="loading()"
        [mode]="jobId() ? 'edit' : 'create'"
        [job]="job()"
        (saveJob)="saveJob($event)"
        (cancelEdit)="goBack()"
      />
    </section>
  `,
})
export class AdminJobFormPageComponent implements OnInit {
  readonly jobId = signal<string | null>(null);
  readonly job = signal<JobOpening | null>(null);
  readonly loading = signal(false);
  readonly message = signal("");

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly authService: AuthService,
    private readonly api: RecruitmentApiService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get("id");
    this.jobId.set(id);

    if (id) {
      this.loadJob(id);
    }
  }

  saveJob(request: JobOpeningRequest): void {
    const session = this.authService.session();

    if (!session) {
      void this.router.navigateByUrl("/login");
      return;
    }

    const id = this.jobId();
    this.loading.set(true);
    this.message.set("");

    const save = id ? this.api.updateJob(session.token, id, request) : this.api.createJob(session.token, request);

    save.subscribe({
      next: () => {
        void this.router.navigateByUrl("/admin/vagas");
      },
      error: () => {
        this.message.set(id ? "Não foi possível atualizar a vaga." : "Não foi possível cadastrar a vaga.");
        this.loading.set(false);
      },
    });
  }

  goBack(): void {
    void this.router.navigateByUrl("/admin/vagas");
  }

  private loadJob(id: string): void {
    this.loading.set(true);
    this.api.findJobById(id).subscribe({
      next: (job) => {
        this.job.set(job);
        this.loading.set(false);
      },
      error: () => {
        this.message.set("Não foi possível carregar a vaga.");
        this.loading.set(false);
      },
    });
  }
}
