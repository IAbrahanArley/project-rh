import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { appConfig } from "./app-config";
import {
  ApplicationStatusRequest,
  AuthResponse,
  Evaluation,
  EvaluationRequest,
  JobApplication,
  JobOpening,
  JobOpeningRequest,
  JobStatus,
  LoginRequest,
  Notification,
  PageResponse,
  UnreadNotificationsResponse,
} from "./models";

@Injectable({ providedIn: "root" })
export class RecruitmentApiService {
  private readonly baseUrl = appConfig.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/api/auth/login`, request);
  }

  listJobs(token: string, status: JobStatus | null = "OPEN"): Observable<PageResponse<JobOpening>> {
    const statusQuery = status ? `status=${status}&` : "";

    return this.http.get<PageResponse<JobOpening>>(`${this.baseUrl}/api/jobs?${statusQuery}page=0&size=20`, {
      headers: this.authHeaders(token),
    });
  }

  createJob(token: string, request: JobOpeningRequest): Observable<JobOpening> {
    return this.http.post<JobOpening>(`${this.baseUrl}/api/jobs`, request, {
      headers: this.authHeaders(token),
    });
  }

  updateJob(token: string, jobId: string, request: JobOpeningRequest): Observable<JobOpening> {
    return this.http.put<JobOpening>(`${this.baseUrl}/api/jobs/${jobId}`, request, {
      headers: this.authHeaders(token),
    });
  }

  cancelJob(token: string, jobId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/jobs/${jobId}`, {
      headers: this.authHeaders(token),
    });
  }

  applyToJob(token: string, jobId: string, motivation: string): Observable<JobApplication> {
    return this.http.post<JobApplication>(
      `${this.baseUrl}/api/jobs/${jobId}/applications`,
      { motivation },
      { headers: this.authHeaders(token) },
    );
  }

  listMyApplications(token: string): Observable<PageResponse<JobApplication>> {
    return this.http.get<PageResponse<JobApplication>>(`${this.baseUrl}/api/applications/me?page=0&size=20`, {
      headers: this.authHeaders(token),
    });
  }

  listJobApplications(token: string, jobId: string): Observable<PageResponse<JobApplication>> {
    return this.http.get<PageResponse<JobApplication>>(`${this.baseUrl}/api/jobs/${jobId}/applications?page=0&size=20`, {
      headers: this.authHeaders(token),
    });
  }

  updateApplicationStatus(
    token: string,
    applicationId: string,
    request: ApplicationStatusRequest,
  ): Observable<JobApplication> {
    return this.http.patch<JobApplication>(`${this.baseUrl}/api/applications/${applicationId}/status`, request, {
      headers: this.authHeaders(token),
    });
  }

  evaluateApplication(token: string, applicationId: string, request: EvaluationRequest): Observable<Evaluation> {
    return this.http.post<Evaluation>(`${this.baseUrl}/api/applications/${applicationId}/evaluation`, request, {
      headers: this.authHeaders(token),
    });
  }

  listNotifications(token: string, read: boolean | null = null): Observable<PageResponse<Notification>> {
    const readQuery = read === null ? "" : `read=${read}&`;

    return this.http.get<PageResponse<Notification>>(`${this.baseUrl}/api/notifications/me?${readQuery}page=0&size=20`, {
      headers: this.authHeaders(token),
    });
  }

  countUnreadNotifications(token: string): Observable<UnreadNotificationsResponse> {
    return this.http.get<UnreadNotificationsResponse>(`${this.baseUrl}/api/notifications/me/unread-count`, {
      headers: this.authHeaders(token),
    });
  }

  markNotificationAsRead(token: string, notificationId: string): Observable<Notification> {
    return this.http.patch<Notification>(
      `${this.baseUrl}/api/notifications/${notificationId}/read`,
      {},
      { headers: this.authHeaders(token) },
    );
  }

  markAllNotificationsAsRead(token: string): Observable<UnreadNotificationsResponse> {
    return this.http.patch<UnreadNotificationsResponse>(
      `${this.baseUrl}/api/notifications/read-all`,
      {},
      { headers: this.authHeaders(token) },
    );
  }

  private authHeaders(token: string): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }
}
