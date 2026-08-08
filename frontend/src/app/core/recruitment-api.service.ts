import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { appConfig } from "./app-config";
import {
  ApplicationStatusRequest,
  AuthResponse,
  CandidateRegisterRequest,
  CompleteResumeUploadRequest,
  Evaluation,
  EvaluationRequest,
  JobApplication,
  JobOpening,
  JobOpeningRequest,
  JobSearchFilters,
  JobStatus,
  LoginRequest,
  Notification,
  PageResponse,
  Resume,
  ResumeDownloadUrlResponse,
  ResumeUploadUrlRequest,
  ResumeUploadUrlResponse,
  UnreadNotificationsResponse,
} from "./models";

@Injectable({ providedIn: "root" })
export class RecruitmentApiService {
  private readonly baseUrl = appConfig.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/api/auth/login`, request);
  }

  registerCandidate(request: CandidateRegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/api/auth/candidate/register`, request);
  }

  getMyResume(token: string): Observable<Resume | null> {
    return this.http
      .get<Resume>(`${this.baseUrl}/api/candidates/me/resume`, {
        headers: this.authHeaders(token),
        observe: "response",
      })
      .pipe(map((response) => response.body ?? null));
  }

  createResumeUploadUrl(token: string, request: ResumeUploadUrlRequest): Observable<ResumeUploadUrlResponse> {
    return this.http.post<ResumeUploadUrlResponse>(`${this.baseUrl}/api/candidates/me/resume/upload-url`, request, {
      headers: this.authHeaders(token),
    });
  }

  uploadResumeFile(uploadUrl: string, file: File): Observable<string> {
    return this.http.put(uploadUrl, file, {
      headers: new HttpHeaders({ "Content-Type": "application/pdf" }),
      responseType: "text",
    });
  }

  completeResumeUpload(token: string, request: CompleteResumeUploadRequest): Observable<Resume> {
    return this.http.post<Resume>(`${this.baseUrl}/api/candidates/me/resume/complete`, request, {
      headers: this.authHeaders(token),
    });
  }

  createMyResumeDownloadUrl(token: string): Observable<ResumeDownloadUrlResponse> {
    return this.http.get<ResumeDownloadUrlResponse>(`${this.baseUrl}/api/candidates/me/resume/download-url`, {
      headers: this.authHeaders(token),
    });
  }

  createCandidateResumeDownloadUrl(token: string, candidateId: string): Observable<ResumeDownloadUrlResponse> {
    return this.http.get<ResumeDownloadUrlResponse>(
      `${this.baseUrl}/api/candidates/${candidateId}/resume/download-url`,
      {
        headers: this.authHeaders(token),
      },
    );
  }

  listJobs(
    filters: JobSearchFilters,
    status: JobStatus | null = "OPEN",
    page = 0,
    size = 10,
  ): Observable<PageResponse<JobOpening>> {
    let params = new HttpParams().set("page", page).set("size", size);

    if (status) {
      params = params.set("status", status);
    }

    if (filters.query.trim()) {
      params = params.set("q", filters.query.trim());
    }

    if (filters.department.trim()) {
      params = params.set("department", filters.department.trim());
    }

    if (filters.location.trim()) {
      params = params.set("location", filters.location.trim());
    }

    return this.http.get<PageResponse<JobOpening>>(`${this.baseUrl}/api/jobs`, {
      params,
    });
  }

  findJobById(jobId: string): Observable<JobOpening> {
    return this.http.get<JobOpening>(`${this.baseUrl}/api/jobs/${jobId}`);
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

  listJobApplications(token: string, jobId: string, page = 0, size = 10): Observable<PageResponse<JobApplication>> {
    return this.http.get<PageResponse<JobApplication>>(`${this.baseUrl}/api/jobs/${jobId}/applications?page=${page}&size=${size}`, {
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
