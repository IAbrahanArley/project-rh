export type Role = "ADMIN" | "CANDIDATE";
export type JobStatus = "OPEN" | "CLOSED" | "CANCELLED";
export type ApplicationStatus = "PENDING" | "IN_REVIEW" | "APPROVED" | "REJECTED" | "WITHDRAWN";

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface AuthenticatedUser {
  id: string;
  username: string;
  fullName: string;
  email: string;
  department: string;
  role: Role;
}

export interface AuthResponse {
  tokenType: "Bearer";
  accessToken: string;
  expiresInSeconds: number;
  user: AuthenticatedUser;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface CandidateRegisterRequest {
  username: string;
  fullName: string;
  email: string;
  password: string;
}

export interface JobOpening {
  id: string;
  title: string;
  description: string;
  requirements: string;
  department: string;
  location: string;
  status: JobStatus;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface JobOpeningRequest {
  title: string;
  description: string;
  requirements: string;
  department: string;
  location: string;
  status: JobStatus;
}

export interface JobApplication {
  id: string;
  jobId: string;
  jobTitle: string;
  candidateId: string;
  candidateName: string;
  candidateDepartment: string;
  motivation: string;
  status: ApplicationStatus;
  feedback: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationStatusRequest {
  status: ApplicationStatus;
  feedback: string | null;
}

export interface EvaluationRequest {
  score: number;
  recommended: boolean;
  comments: string;
}

export interface Evaluation {
  id: string;
  applicationId: string;
  evaluatorName: string;
  score: number;
  recommended: boolean;
  comments: string;
  createdAt: string;
  updatedAt: string;
}

export interface Notification {
  id: string;
  subject: string;
  message: string;
  read: boolean;
  createdAt: string;
}

export interface UnreadNotificationsResponse {
  unreadCount: number;
}
