import { Component, computed, OnInit, signal } from "@angular/core";
import { Router, RouterLink } from "@angular/router";
import { AuthService } from "../core/auth.service";
import { JobOpening, JobSearchFilters, JobStatus } from "../core/models";
import { RecruitmentApiService } from "../core/recruitment-api.service";
import { JobDetailComponent } from "../jobs/job-detail.component";
import { JobListComponent } from "../jobs/job-list.component";

@Component({
  selector: "app-admin-jobs-page",
  standalone: true,
  imports: [JobDetailComponent, JobListComponent, RouterLink],
  template: `
    <header class="page-heading">
      <div>
        <span class="eyebrow">Vagas</span>
        <h2>Gerencie oportunidades</h2>
        <p>Busque, filtre, visualize, edite ou cancele vagas internas.</p>
      </div>

      <a class="button-link" routerLink="/admin/vagas/nova">Nova vaga</a>
    </header>

    @if (message()) {
      <p class="alert">{{ message() }}</p>
    }

    <section class="jobs-page-grid">
      <app-job-list
        [jobs]="jobs()"
        [filters]="filters()"
        [selectedJobId]="selectedJobId()"
        [selectedStatus]="status()"
        [page]="page()"
        [totalPages]="totalPages()"
        [first]="first()"
        [last]="last()"
        [canFilterStatus]="true"
        [loading]="loading()"
        (selectJob)="selectJob($event)"
        (statusChange)="changeStatus($event)"
        (filtersChange)="changeFilters($event)"
        (pageChange)="changePage($event)"
      />

      <app-job-detail
        [job]="selectedJob()"
        [canManage]="true"
        [loading]="loading()"
        (editJob)="editJob($event)"
        (cancelJob)="cancelJob($event)"
      />
    </section>
  `,
})
export class AdminJobsPageComponent implements OnInit {
  readonly jobs = signal<JobOpening[]>([]);
  readonly selectedJobId = signal<string | null>(null);
  readonly filters = signal<JobSearchFilters>({ query: "", department: "", location: "" });
  readonly status = signal<JobStatus | null>("OPEN");
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly first = signal(true);
  readonly last = signal(true);
  readonly loading = signal(false);
  readonly message = signal("");

  readonly selectedJob = computed(() => {
    const selectedId = this.selectedJobId();
    return this.jobs().find((job) => job.id === selectedId) ?? this.jobs()[0] ?? null;
  });

  constructor(
    private readonly authService: AuthService,
    private readonly api: RecruitmentApiService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.loadJobs();
  }

  loadJobs(): void {
    this.loading.set(true);
    this.message.set("");

    this.api.listJobs(this.filters(), this.status(), this.page()).subscribe({
      next: (jobs) => {
        const currentSelectedId = this.selectedJobId();
        const selectedJobId = jobs.content.some((job) => job.id === currentSelectedId)
          ? currentSelectedId
          : (jobs.content[0]?.id ?? null);

        this.jobs.set(jobs.content);
        this.page.set(jobs.page);
        this.totalPages.set(jobs.totalPages);
        this.first.set(jobs.first);
        this.last.set(jobs.last);
        this.selectedJobId.set(selectedJobId);
        this.loading.set(false);
      },
      error: () => {
        this.message.set("Não foi possível carregar as vagas.");
        this.loading.set(false);
      },
    });
  }

  selectJob(jobId: string): void {
    this.selectedJobId.set(jobId);
  }

  changeStatus(status: JobStatus | null): void {
    if (this.status() === status) {
      return;
    }

    this.status.set(status);
    this.resetListState();
    this.loadJobs();
  }

  changeFilters(filters: JobSearchFilters): void {
    this.filters.set(filters);
    this.resetListState();
    this.loadJobs();
  }

  changePage(page: number): void {
    if (page < 0 || page === this.page() || page >= this.totalPages()) {
      return;
    }

    this.page.set(page);
    this.selectedJobId.set(null);
    this.loadJobs();
  }

  editJob(job: JobOpening): void {
    void this.router.navigate(["/admin/vagas", job.id, "editar"]);
  }

  cancelJob(job: JobOpening): void {
    const session = this.authService.session();

    if (!session) {
      void this.router.navigateByUrl("/login");
      return;
    }

    const shouldCancel = window.confirm(`Cancelar a vaga "${job.title}"? Esta ação mantém o histórico da vaga.`);

    if (!shouldCancel) {
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.cancelJob(session.token, job.id).subscribe({
      next: () => {
        this.message.set("Vaga cancelada com sucesso.");
        this.loadJobs();
      },
      error: () => {
        this.message.set("Não foi possível cancelar a vaga.");
        this.loading.set(false);
      },
    });
  }

  private resetListState(): void {
    this.page.set(0);
    this.selectedJobId.set(null);
  }
}
