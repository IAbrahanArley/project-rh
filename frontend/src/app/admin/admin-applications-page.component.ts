import { Component, computed, OnInit, signal } from "@angular/core";
import { Router } from "@angular/router";
import {
  AdminApplicationPanelComponent,
  ApplicationEvaluationSubmit,
  ApplicationStatusChange,
} from "../applications/admin-application-panel.component";
import { AuthService } from "../core/auth.service";
import { JobApplication, JobOpening, JobSearchFilters, JobStatus } from "../core/models";
import { RecruitmentApiService } from "../core/recruitment-api.service";
import { JobListComponent } from "../jobs/job-list.component";

@Component({
  selector: "app-admin-applications-page",
  standalone: true,
  imports: [AdminApplicationPanelComponent, JobListComponent],
  template: `
    <header class="page-heading">
      <div>
        <span class="eyebrow">Candidaturas</span>
        <h2>Gestão de candidatos</h2>
        <p>Selecione uma vaga para avaliar candidatos, status, feedback e currículo.</p>
      </div>
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

      <app-admin-application-panel
        [applications]="applications()"
        [job]="selectedJob()"
        [page]="applicationsPage()"
        [totalPages]="applicationsTotalPages()"
        [first]="applicationsFirst()"
        [last]="applicationsLast()"
        [loading]="loading()"
        (statusChange)="updateApplicationStatus($event)"
        (evaluationSubmit)="evaluateApplication($event)"
        (resumeDownload)="openCandidateResume($event)"
        (pageChange)="changeApplicationsPage($event)"
      />
    </section>
  `,
})
export class AdminApplicationsPageComponent implements OnInit {
  readonly jobs = signal<JobOpening[]>([]);
  readonly applications = signal<JobApplication[]>([]);
  readonly applicationsPage = signal(0);
  readonly applicationsTotalPages = signal(0);
  readonly applicationsFirst = signal(true);
  readonly applicationsLast = signal(true);
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

        if (selectedJobId) {
          this.applicationsPage.set(0);
          this.loadApplications(selectedJobId);
        } else {
          this.applications.set([]);
          this.resetApplicationsPage();
        }
      },
      error: () => {
        this.message.set("Não foi possível carregar as vagas.");
        this.loading.set(false);
      },
    });
  }

  selectJob(jobId: string): void {
    this.selectedJobId.set(jobId);
    this.applicationsPage.set(0);
    this.loadApplications(jobId);
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

  changeApplicationsPage(page: number): void {
    const jobId = this.selectedJobId();

    if (!jobId || page < 0 || page === this.applicationsPage() || page >= this.applicationsTotalPages()) {
      return;
    }

    this.applicationsPage.set(page);
    this.loadApplications(jobId);
  }

  updateApplicationStatus(change: ApplicationStatusChange): void {
    const session = this.authService.session();

    if (!session) {
      void this.router.navigateByUrl("/login");
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.updateApplicationStatus(session.token, change.applicationId, change.request).subscribe({
      next: () => this.reloadSelectedApplications(),
      error: () => {
        this.message.set("Não foi possível atualizar o status da candidatura.");
        this.loading.set(false);
      },
    });
  }

  evaluateApplication(change: ApplicationEvaluationSubmit): void {
    const session = this.authService.session();

    if (!session) {
      void this.router.navigateByUrl("/login");
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.evaluateApplication(session.token, change.applicationId, change.request).subscribe({
      next: () => {
        this.message.set("Avaliação registrada com sucesso.");
        this.loading.set(false);
      },
      error: () => {
        this.message.set("Não foi possível registrar a avaliação.");
        this.loading.set(false);
      },
    });
  }

  openCandidateResume(candidateId: string): void {
    const session = this.authService.session();

    if (!session) {
      void this.router.navigateByUrl("/login");
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.createCandidateResumeDownloadUrl(session.token, candidateId).subscribe({
      next: (response) => {
        window.open(response.downloadUrl, "_blank", "noopener,noreferrer");
        this.loading.set(false);
      },
      error: () => {
        this.message.set("Não foi possível abrir o currículo do candidato.");
        this.loading.set(false);
      },
    });
  }

  private loadApplications(jobId: string): void {
    const session = this.authService.session();

    if (!session) {
      void this.router.navigateByUrl("/login");
      return;
    }

    this.loading.set(true);

    this.api.listJobApplications(session.token, jobId, this.applicationsPage()).subscribe({
      next: (applications) => {
        this.applications.set(applications.content);
        this.applicationsPage.set(applications.page);
        this.applicationsTotalPages.set(applications.totalPages);
        this.applicationsFirst.set(applications.first);
        this.applicationsLast.set(applications.last);
        this.loading.set(false);
      },
      error: () => {
        this.applications.set([]);
        this.message.set("Não foi possível carregar as candidaturas da vaga.");
        this.loading.set(false);
      },
    });
  }

  private reloadSelectedApplications(): void {
    const jobId = this.selectedJobId();

    if (!jobId) {
      this.loading.set(false);
      return;
    }

    this.loadApplications(jobId);
  }

  private resetListState(): void {
    this.page.set(0);
    this.selectedJobId.set(null);
    this.applications.set([]);
    this.resetApplicationsPage();
  }

  private resetApplicationsPage(): void {
    this.applicationsPage.set(0);
    this.applicationsTotalPages.set(0);
    this.applicationsFirst.set(true);
    this.applicationsLast.set(true);
  }
}
