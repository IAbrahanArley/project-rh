import { Component, computed, OnInit, signal } from "@angular/core";
import { Router } from "@angular/router";
import { switchMap } from "rxjs";
import { AuthService } from "../core/auth.service";
import { JobOpening, JobSearchFilters } from "../core/models";
import { RecruitmentApiService } from "../core/recruitment-api.service";
import { JobApplicationSubmit, JobDetailComponent } from "../jobs/job-detail.component";
import { JobListComponent } from "../jobs/job-list.component";

@Component({
  selector: "app-candidate-jobs-page",
  standalone: true,
  imports: [JobDetailComponent, JobListComponent],
  template: `
    <header class="page-heading">
      <div>
        <span class="eyebrow">Vagas</span>
        <h2>Encontre sua próxima oportunidade</h2>
        <p>Filtre as vagas abertas e anexe o currículo no momento da candidatura.</p>
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
        [selectedStatus]="'OPEN'"
        [page]="page()"
        [totalPages]="totalPages()"
        [first]="first()"
        [last]="last()"
        [loading]="loading()"
        (selectJob)="selectJob($event)"
        (filtersChange)="changeFilters($event)"
        (pageChange)="changePage($event)"
      />

      <app-job-detail
        [job]="selectedJob()"
        [canApply]="true"
        [requireResumeOnApply]="true"
        [loading]="loading()"
        (applyToJob)="applyToSelectedJob($event)"
      />
    </section>
  `,
})
export class CandidateJobsPageComponent implements OnInit {
  readonly jobs = signal<JobOpening[]>([]);
  readonly selectedJobId = signal<string | null>(null);
  readonly filters = signal<JobSearchFilters>({ query: "", department: "", location: "" });
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
    this.message.set("");

    this.api.listJobs(this.filters(), "OPEN", this.page()).subscribe({
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

  changeFilters(filters: JobSearchFilters): void {
    this.filters.set(filters);
    this.page.set(0);
    this.selectedJobId.set(null);
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

  applyToSelectedJob(submit: JobApplicationSubmit): void {
    const session = this.authService.session();
    const job = this.selectedJob();
    const file = submit.resumeFile;

    if (!session || !job || !file) {
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api
      .createResumeUploadUrl(session.token, {
        fileName: file.name,
        contentType: "application/pdf",
        sizeBytes: file.size,
      })
      .pipe(
        switchMap((upload) =>
          this.api.uploadResumeFile(upload.uploadUrl, file).pipe(
            switchMap(() =>
              this.api.completeResumeUpload(session.token, {
                storageKey: upload.storageKey,
                fileName: file.name,
                contentType: upload.requiredContentType,
                sizeBytes: file.size,
              }),
            ),
          ),
        ),
        switchMap(() => this.api.applyToJob(session.token, job.id, submit.motivation)),
      )
      .subscribe({
        next: () => {
          this.message.set("Candidatura enviada com currículo anexado.");
          this.loading.set(false);
          void this.router.navigateByUrl("/candidaturas");
        },
        error: () => {
          this.message.set("Não foi possível enviar a candidatura. Verifique o PDF e tente novamente.");
          this.loading.set(false);
        },
      });
  }
}
