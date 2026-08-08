import { Component, computed, OnInit, signal } from "@angular/core";
import { Router } from "@angular/router";
import { catchError, forkJoin, of } from "rxjs";
import {
  AdminApplicationPanelComponent,
  ApplicationEvaluationSubmit,
  ApplicationStatusChange,
} from "../applications/admin-application-panel.component";
import { ApplicationListComponent } from "../applications/application-list.component";
import { AuthService } from "../core/auth.service";
import { JobApplication, JobOpening, JobOpeningRequest, JobStatus, Notification, PageResponse } from "../core/models";
import { RecruitmentApiService } from "../core/recruitment-api.service";
import { SummaryCardComponent } from "./summary-card.component";
import { JobDetailComponent } from "../jobs/job-detail.component";
import { JobFormComponent } from "../jobs/job-form.component";
import { JobListComponent } from "../jobs/job-list.component";
import { TopbarComponent } from "../layout/topbar.component";
import { NotificationListComponent, NotificationReadFilter } from "../notifications/notification-list.component";

@Component({
  selector: "app-dashboard",
  standalone: true,
  imports: [
    AdminApplicationPanelComponent,
    ApplicationListComponent,
    JobDetailComponent,
    JobFormComponent,
    JobListComponent,
    NotificationListComponent,
    SummaryCardComponent,
    TopbarComponent,
  ],
  template: `
    <main class="app-shell">
      @if (session()) {
        <app-topbar [user]="session()!.user" (logout)="logout()" />
      }

      @if (message()) {
        <p class="alert">{{ message() }}</p>
      }

      <section class="summary-grid">
        <app-summary-card [label]="session()?.user?.role === 'ADMIN' ? 'Vagas no filtro' : 'Vagas abertas'" [value]="jobs().length" />
        <app-summary-card
          [label]="session()?.user?.role === 'ADMIN' ? 'Candidaturas da vaga' : 'Minhas candidaturas'"
          [value]="session()?.user?.role === 'ADMIN' ? adminApplications().length : applications().length"
        />
        <app-summary-card label="Nao lidas" [value]="unreadCount()" />
      </section>

      <section class="content-grid">
        <app-job-list
          [jobs]="jobs()"
          [selectedJobId]="selectedJobId()"
          [selectedStatus]="jobStatusFilter()"
          [canFilterStatus]="session()?.user?.role === 'ADMIN'"
          [loading]="loading()"
          (selectJob)="selectJob($event)"
          (statusChange)="changeJobStatusFilter($event)"
        />

        <app-job-detail
          [job]="selectedJob()"
          [canApply]="session()?.user?.role === 'CANDIDATE'"
          [canManage]="session()?.user?.role === 'ADMIN'"
          [loading]="loading()"
          (applyToJob)="applyToSelectedJob($event)"
          (editJob)="startEditingJob($event)"
          (cancelJob)="cancelJob($event)"
        />

        @if (session()?.user?.role === "ADMIN") {
          <app-job-form
            [loading]="loading()"
            [mode]="editingJob() ? 'edit' : 'create'"
            [job]="editingJob()"
            (saveJob)="saveJob($event)"
            (cancelEdit)="stopEditingJob()"
          />
        }

        @if (session()?.user?.role === "ADMIN") {
          <app-admin-application-panel
            [applications]="adminApplications()"
            [job]="selectedJob()"
            [loading]="loading()"
            (statusChange)="updateApplicationStatus($event)"
            (evaluationSubmit)="evaluateApplication($event)"
          />
        } @else {
          <app-application-list [applications]="applications()" />
        }

        <app-notification-list
          [notifications]="notifications()"
          [unreadCount]="unreadCount()"
          [readFilter]="notificationReadFilter()"
          [loading]="loading()"
          (markAsRead)="markNotificationAsRead($event)"
          (markAllAsRead)="markAllNotificationsAsRead()"
          (readFilterChange)="changeNotificationReadFilter($event)"
        />
      </section>
    </main>
  `,
})
export class DashboardComponent implements OnInit {
  readonly session = this.authService.session;
  readonly jobs = signal<JobOpening[]>([]);
  readonly applications = signal<JobApplication[]>([]);
  readonly adminApplications = signal<JobApplication[]>([]);
  readonly notifications = signal<Notification[]>([]);
  readonly unreadCount = signal(0);
  readonly selectedJobId = signal<string | null>(null);
  readonly jobStatusFilter = signal<JobStatus | null>("OPEN");
  readonly notificationReadFilter = signal<NotificationReadFilter>(null);
  readonly editingJob = signal<JobOpening | null>(null);
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
    this.loadDashboard();
  }

  loadDashboard(): void {
    const session = this.session();

    if (!session) {
      void this.router.navigateByUrl("/login");
      return;
    }

    this.loading.set(true);
    this.message.set("");

    const isAdmin = session.user.role === "ADMIN";

    forkJoin({
      jobs: this.api.listJobs(session.token, isAdmin ? this.jobStatusFilter() : "OPEN"),
      applications: isAdmin
        ? of(this.emptyPage<JobApplication>())
        : this.api.listMyApplications(session.token).pipe(catchError(() => of(this.emptyPage<JobApplication>()))),
      notifications: this.api.listNotifications(session.token, this.notificationReadFilter()),
      unread: this.api.countUnreadNotifications(session.token),
    }).subscribe({
      next: ({ jobs, applications, notifications, unread }) => {
        const currentSelectedId = this.selectedJobId();
        const selectedJobId = jobs.content.some((job) => job.id === currentSelectedId)
          ? currentSelectedId
          : (jobs.content[0]?.id ?? null);

        this.jobs.set(jobs.content);
        this.applications.set(applications.content);
        this.notifications.set(notifications.content);
        this.unreadCount.set(unread.unreadCount);
        this.selectedJobId.set(selectedJobId);
        this.syncEditingJob(jobs.content);
        this.adminApplications.set([]);
        this.loading.set(false);

        if (isAdmin && selectedJobId) {
          this.loadJobApplications(selectedJobId);
        }
      },
      error: () => {
        this.message.set("Nao foi possivel carregar os dados da API.");
        this.loading.set(false);
      },
    });
  }

  selectJob(jobId: string): void {
    this.selectedJobId.set(jobId);
    this.stopEditingJob();

    if (this.session()?.user?.role === "ADMIN") {
      this.loadJobApplications(jobId);
    }
  }

  changeJobStatusFilter(status: JobStatus | null): void {
    if (this.jobStatusFilter() === status) {
      return;
    }

    this.jobStatusFilter.set(status);
    this.selectedJobId.set(null);
    this.stopEditingJob();
    this.loadDashboard();
  }

  saveJob(request: JobOpeningRequest): void {
    const job = this.editingJob();

    if (job) {
      this.updateJob(job.id, request);
      return;
    }

    this.createJob(request);
  }

  startEditingJob(job: JobOpening): void {
    this.editingJob.set(job);
    this.message.set("");
  }

  stopEditingJob(): void {
    this.editingJob.set(null);
  }

  cancelJob(job: JobOpening): void {
    const session = this.session();

    if (!session) {
      return;
    }

    const shouldCancel = window.confirm(`Cancelar a vaga "${job.title}"? Esta acao mantem o historico da vaga.`);

    if (!shouldCancel) {
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.cancelJob(session.token, job.id).subscribe({
      next: () => {
        this.message.set("Vaga cancelada com sucesso.");
        this.stopEditingJob();
        this.loadDashboard();
      },
      error: () => {
        this.message.set("Nao foi possivel cancelar a vaga.");
        this.loading.set(false);
      },
    });
  }

  private createJob(request: JobOpeningRequest): void {
    const session = this.session();

    if (!session) {
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.createJob(session.token, request).subscribe({
      next: (job) => {
        this.message.set("Vaga cadastrada com sucesso.");
        this.selectedJobId.set(job.id);
        this.jobStatusFilter.set(job.status);
        this.loadDashboard();
      },
      error: () => {
        this.message.set("Nao foi possivel cadastrar a vaga.");
        this.loading.set(false);
      },
    });
  }

  private updateJob(jobId: string, request: JobOpeningRequest): void {
    const session = this.session();

    if (!session) {
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.updateJob(session.token, jobId, request).subscribe({
      next: (job) => {
        this.message.set("Vaga atualizada com sucesso.");
        this.selectedJobId.set(job.id);
        this.jobStatusFilter.set(job.status);
        this.stopEditingJob();
        this.loadDashboard();
      },
      error: () => {
        this.message.set("Nao foi possivel atualizar a vaga.");
        this.loading.set(false);
      },
    });
  }

  applyToSelectedJob(motivation: string): void {
    const session = this.session();
    const job = this.selectedJob();

    if (!session || !job) {
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.applyToJob(session.token, job.id, motivation).subscribe({
      next: () => {
        this.message.set("Candidatura enviada com sucesso.");
        this.loadDashboard();
      },
      error: () => {
        this.message.set("Nao foi possivel enviar a candidatura.");
        this.loading.set(false);
      },
    });
  }

  updateApplicationStatus(change: ApplicationStatusChange): void {
    const session = this.session();

    if (!session) {
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.updateApplicationStatus(session.token, change.applicationId, change.request).subscribe({
      next: () => {
        this.message.set("Status da candidatura atualizado com sucesso.");
        this.reloadSelectedJobApplications();
      },
      error: () => {
        this.message.set("Nao foi possivel atualizar o status da candidatura.");
        this.loading.set(false);
      },
    });
  }

  evaluateApplication(change: ApplicationEvaluationSubmit): void {
    const session = this.session();

    if (!session) {
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.evaluateApplication(session.token, change.applicationId, change.request).subscribe({
      next: () => {
        this.message.set("Avaliacao registrada com sucesso.");
        this.loading.set(false);
      },
      error: () => {
        this.message.set("Nao foi possivel registrar a avaliacao.");
        this.loading.set(false);
      },
    });
  }

  changeNotificationReadFilter(read: NotificationReadFilter): void {
    if (this.notificationReadFilter() === read) {
      return;
    }

    this.notificationReadFilter.set(read);
    this.loadNotifications();
  }

  markNotificationAsRead(notificationId: string): void {
    const session = this.session();

    if (!session) {
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.markNotificationAsRead(session.token, notificationId).subscribe({
      next: () => {
        this.message.set("Notificacao marcada como lida.");
        this.loadNotifications();
      },
      error: () => {
        this.message.set("Nao foi possivel atualizar a notificacao.");
        this.loading.set(false);
      },
    });
  }

  markAllNotificationsAsRead(): void {
    const session = this.session();

    if (!session) {
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.markAllNotificationsAsRead(session.token).subscribe({
      next: () => {
        this.message.set("Notificacoes marcadas como lidas.");
        this.loadNotifications();
      },
      error: () => {
        this.message.set("Nao foi possivel marcar as notificacoes como lidas.");
        this.loading.set(false);
      },
    });
  }

  logout(): void {
    this.authService.signOut();
    void this.router.navigateByUrl("/login");
  }

  private loadJobApplications(jobId: string): void {
    const session = this.session();

    if (!session) {
      return;
    }

    this.loading.set(true);

    this.api.listJobApplications(session.token, jobId).subscribe({
      next: (applications) => {
        this.adminApplications.set(applications.content);
        this.loading.set(false);
      },
      error: () => {
        this.adminApplications.set([]);
        this.message.set("Nao foi possivel carregar as candidaturas da vaga.");
        this.loading.set(false);
      },
    });
  }

  private loadNotifications(): void {
    const session = this.session();

    if (!session) {
      return;
    }

    this.loading.set(true);

    forkJoin({
      notifications: this.api.listNotifications(session.token, this.notificationReadFilter()),
      unread: this.api.countUnreadNotifications(session.token),
    }).subscribe({
      next: ({ notifications, unread }) => {
        this.notifications.set(notifications.content);
        this.unreadCount.set(unread.unreadCount);
        this.loading.set(false);
      },
      error: () => {
        this.message.set("Nao foi possivel carregar as notificacoes.");
        this.loading.set(false);
      },
    });
  }

  private reloadSelectedJobApplications(): void {
    const selectedJobId = this.selectedJobId();

    if (!selectedJobId) {
      this.loading.set(false);
      return;
    }

    this.loadJobApplications(selectedJobId);
  }

  private emptyPage<T>(): PageResponse<T> {
    return {
      content: [],
      page: 0,
      size: 0,
      totalElements: 0,
      totalPages: 0,
      first: true,
      last: true,
    };
  }

  private syncEditingJob(jobs: JobOpening[]): void {
    const editingJob = this.editingJob();

    if (!editingJob) {
      return;
    }

    this.editingJob.set(jobs.find((job) => job.id === editingJob.id) ?? null);
  }
}
