import { Component, OnInit, signal } from "@angular/core";
import { Router, RouterLink } from "@angular/router";
import { catchError, forkJoin, of } from "rxjs";
import { ApplicationListComponent } from "../applications/application-list.component";
import { AuthService } from "../core/auth.service";
import { JobApplication, JobOpening, Notification, PageResponse } from "../core/models";
import { RecruitmentApiService } from "../core/recruitment-api.service";
import { SummaryCardComponent } from "../dashboard/summary-card.component";
import { NotificationListComponent, NotificationReadFilter } from "../notifications/notification-list.component";

@Component({
  selector: "app-candidate-home",
  standalone: true,
  imports: [ApplicationListComponent, NotificationListComponent, RouterLink, SummaryCardComponent],
  template: `
    <header class="page-heading">
      <div>
        <span class="eyebrow">Painel</span>
        <h2>Resumo da sua jornada</h2>
        <p>Acompanhe vagas abertas, candidaturas e notificações recentes.</p>
      </div>

      <a class="button-link" routerLink="/vagas">Ver vagas</a>
    </header>

    @if (message()) {
      <p class="alert">{{ message() }}</p>
    }

    <section class="summary-grid">
      <app-summary-card label="Vagas abertas" [value]="openJobsCount()" />
      <app-summary-card label="Minhas candidaturas" [value]="applications().length" />
      <app-summary-card label="Não lidas" [value]="unreadCount()" />
    </section>

    <section class="dashboard-sections">
      <app-application-list [applications]="applications().slice(0, 3)" />

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
  `,
})
export class CandidateHomeComponent implements OnInit {
  readonly applications = signal<JobApplication[]>([]);
  readonly notifications = signal<Notification[]>([]);
  readonly unreadCount = signal(0);
  readonly openJobsCount = signal(0);
  readonly notificationReadFilter = signal<NotificationReadFilter>(null);
  readonly loading = signal(false);
  readonly message = signal("");

  constructor(
    private readonly authService: AuthService,
    private readonly api: RecruitmentApiService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
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

    forkJoin({
      jobs: this.api.listJobs({ query: "", department: "", location: "" }, "OPEN", 0, 1),
      applications: this.api.listMyApplications(session.token).pipe(catchError(() => of(this.emptyPage<JobApplication>()))),
      notifications: this.api.listNotifications(session.token, this.notificationReadFilter()),
      unread: this.api.countUnreadNotifications(session.token),
    }).subscribe({
      next: ({ jobs, applications, notifications, unread }) => {
        this.openJobsCount.set(jobs.totalElements);
        this.applications.set(applications.content);
        this.notifications.set(notifications.content);
        this.unreadCount.set(unread.unreadCount);
        this.loading.set(false);
      },
      error: () => {
        this.message.set("Não foi possível carregar o painel.");
        this.loading.set(false);
      },
    });
  }

  changeNotificationReadFilter(read: NotificationReadFilter): void {
    if (this.notificationReadFilter() === read) {
      return;
    }

    this.notificationReadFilter.set(read);
    this.loadDashboard();
  }

  markNotificationAsRead(notificationId: string): void {
    const session = this.authService.session();

    if (!session) {
      return;
    }

    this.loading.set(true);
    this.api.markNotificationAsRead(session.token, notificationId).subscribe({
      next: () => this.loadDashboard(),
      error: () => {
        this.message.set("Não foi possível atualizar a notificação.");
        this.loading.set(false);
      },
    });
  }

  markAllNotificationsAsRead(): void {
    const session = this.authService.session();

    if (!session) {
      return;
    }

    this.loading.set(true);
    this.api.markAllNotificationsAsRead(session.token).subscribe({
      next: () => this.loadDashboard(),
      error: () => {
        this.message.set("Não foi possível marcar as notificações.");
        this.loading.set(false);
      },
    });
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
}
