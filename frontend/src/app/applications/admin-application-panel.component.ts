import { Component, EventEmitter, Input, OnChanges, Output } from "@angular/core";
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { applicationStatusLabel } from "../core/display-labels";
import { ApplicationStatus, ApplicationStatusRequest, EvaluationRequest, JobApplication, JobOpening } from "../core/models";

type StatusForm = FormGroup<{
  status: FormControl<ApplicationStatus>;
  feedback: FormControl<string>;
}>;

type EvaluationForm = FormGroup<{
  score: FormControl<number>;
  recommended: FormControl<boolean>;
  comments: FormControl<string>;
}>;

export interface ApplicationStatusChange {
  applicationId: string;
  request: ApplicationStatusRequest;
}

export interface ApplicationEvaluationSubmit {
  applicationId: string;
  request: EvaluationRequest;
}

@Component({
  selector: "app-admin-application-panel",
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="panel admin-applications-panel">
      <div class="panel-header">
        <div>
          <h2>Candidaturas da vaga</h2>
          @if (job) {
            <span>{{ job.title }}</span>
          }
        </div>
        @if (loading) {
          <span>Atualizando...</span>
        }
      </div>

      <div class="stack">
        @for (application of applications; track application.id) {
          <article class="application-review">
            <div class="application-review-header">
              <div>
                <strong>{{ application.candidateName }}</strong>
                <span>{{ application.candidateDepartment }}</span>
              </div>
              <span class="status">{{ applicationStatusLabel(application.status) }}</span>
            </div>

            <p>{{ application.motivation }}</p>

            @if (application.feedback) {
              <p class="feedback">{{ application.feedback }}</p>
            }

            <form class="review-form" [formGroup]="statusFormFor(application)" (ngSubmit)="submitStatus(application)">
              <label>
                Status
                <select formControlName="status">
                  @for (status of statusOptions; track status) {
                    <option [value]="status">{{ applicationStatusLabel(status) }}</option>
                  }
                </select>
              </label>

              <label>
                Feedback
                <textarea formControlName="feedback" placeholder="Retorno para o colaborador"></textarea>
              </label>

              <button [disabled]="statusFormFor(application).invalid || loading">Salvar andamento</button>
            </form>

            <form
              class="review-form evaluation-form"
              [formGroup]="evaluationFormFor(application)"
              (ngSubmit)="submitEvaluation(application)"
            >
              <label>
                Nota
                <input formControlName="score" type="number" min="1" max="5" />
              </label>

              <label class="checkbox-label">
                <input formControlName="recommended" type="checkbox" />
                Recomendado
              </label>

              <label class="wide-field">
                Comentarios internos
                <textarea formControlName="comments" placeholder="Observacoes da avaliacao"></textarea>
              </label>

              <button [disabled]="evaluationFormFor(application).invalid || loading">Registrar avaliacao</button>
            </form>
          </article>
        } @empty {
          <p>Nenhuma candidatura encontrada para esta vaga.</p>
        }
      </div>
    </div>
  `,
})
export class AdminApplicationPanelComponent implements OnChanges {
  @Input({ required: true }) applications: JobApplication[] = [];
  @Input() job: JobOpening | null = null;
  @Input() loading = false;
  @Output() statusChange = new EventEmitter<ApplicationStatusChange>();
  @Output() evaluationSubmit = new EventEmitter<ApplicationEvaluationSubmit>();

  readonly statusOptions: ApplicationStatus[] = ["PENDING", "IN_REVIEW", "APPROVED", "REJECTED", "WITHDRAWN"];
  protected readonly applicationStatusLabel = applicationStatusLabel;

  private readonly statusForms = new Map<string, StatusForm>();
  private readonly evaluationForms = new Map<string, EvaluationForm>();

  constructor(private readonly formBuilder: FormBuilder) {}

  ngOnChanges(): void {
    const applicationIds = new Set(this.applications.map((application) => application.id));

    for (const application of this.applications) {
      if (!this.statusForms.has(application.id)) {
        this.statusForms.set(
          application.id,
          this.formBuilder.nonNullable.group({
            status: [application.status, [Validators.required]],
            feedback: [application.feedback ?? "", [Validators.maxLength(2000)]],
          }),
        );
      }

      if (!this.evaluationForms.has(application.id)) {
        this.evaluationForms.set(
          application.id,
          this.formBuilder.nonNullable.group({
            score: [3, [Validators.required, Validators.min(1), Validators.max(5)]],
            recommended: [false],
            comments: ["", [Validators.required, Validators.maxLength(2000)]],
          }),
        );
      }
    }

    for (const applicationId of this.statusForms.keys()) {
      if (!applicationIds.has(applicationId)) {
        this.statusForms.delete(applicationId);
        this.evaluationForms.delete(applicationId);
      }
    }
  }

  statusFormFor(application: JobApplication): StatusForm {
    return this.statusForms.get(application.id) ?? this.createStatusForm(application);
  }

  evaluationFormFor(application: JobApplication): EvaluationForm {
    return this.evaluationForms.get(application.id) ?? this.createEvaluationForm(application.id);
  }

  submitStatus(application: JobApplication): void {
    const form = this.statusFormFor(application);

    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }

    const rawValue = form.getRawValue();

    this.statusChange.emit({
      applicationId: application.id,
      request: {
        status: rawValue.status,
        feedback: rawValue.feedback.trim() || null,
      },
    });
  }

  submitEvaluation(application: JobApplication): void {
    const form = this.evaluationFormFor(application);

    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }

    const rawValue = form.getRawValue();

    this.evaluationSubmit.emit({
      applicationId: application.id,
      request: {
        ...rawValue,
        comments: rawValue.comments.trim(),
      },
    });
  }

  private createStatusForm(application: JobApplication): StatusForm {
    const form = this.formBuilder.nonNullable.group({
      status: [application.status, [Validators.required]],
      feedback: [application.feedback ?? "", [Validators.maxLength(2000)]],
    });

    this.statusForms.set(application.id, form);
    return form;
  }

  private createEvaluationForm(applicationId: string): EvaluationForm {
    const form = this.formBuilder.nonNullable.group({
      score: [3, [Validators.required, Validators.min(1), Validators.max(5)]],
      recommended: [false],
      comments: ["", [Validators.required, Validators.maxLength(2000)]],
    });

    this.evaluationForms.set(applicationId, form);
    return form;
  }
}
