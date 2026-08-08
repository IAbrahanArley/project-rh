import { ApplicationStatus, JobStatus, Role } from "./models";

const roleLabels: Record<Role, string> = {
  ADMIN: "RH",
  CANDIDATE: "Colaborador",
};

const jobStatusLabels: Record<JobStatus, string> = {
  OPEN: "Aberta",
  CLOSED: "Fechada",
  CANCELLED: "Cancelada",
};

const applicationStatusLabels: Record<ApplicationStatus, string> = {
  PENDING: "Pendente",
  IN_REVIEW: "Em analise",
  APPROVED: "Aprovada",
  REJECTED: "Rejeitada",
  WITHDRAWN: "Retirada",
};

const sentenceStatusLabels: Record<ApplicationStatus | JobStatus, string> = {
  PENDING: "pendente",
  IN_REVIEW: "em analise",
  APPROVED: "aprovada",
  REJECTED: "rejeitada",
  WITHDRAWN: "retirada",
  OPEN: "aberta",
  CLOSED: "fechada",
  CANCELLED: "cancelada",
};

const statusTokenPattern = /\b(PENDING|IN_REVIEW|APPROVED|REJECTED|WITHDRAWN|OPEN|CLOSED|CANCELLED)\b/g;

export function roleLabel(role: Role): string {
  return roleLabels[role];
}

export function jobStatusLabel(status: JobStatus): string {
  return jobStatusLabels[status];
}

export function applicationStatusLabel(status: ApplicationStatus): string {
  return applicationStatusLabels[status];
}

export function humanizeBackendMessage(message: string): string {
  return message
    .replace(/\best[a\u00e1]\s+como\s+(PENDING|IN_REVIEW|APPROVED|REJECTED|WITHDRAWN|OPEN|CLOSED|CANCELLED)\b/g, (_, status: string) => {
      return `esta ${sentenceStatusLabels[status as ApplicationStatus | JobStatus]}`;
    })
    .replace(statusTokenPattern, (status) => sentenceStatusLabels[status as ApplicationStatus | JobStatus]);
}
