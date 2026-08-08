import { Component, EventEmitter, Input, Output } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { jobStatusLabel } from "../core/display-labels";
import { JobOpening } from "../core/models";

export interface JobApplicationSubmit {
  motivation: string;
  resumeFile: File | null;
}

const maxResumeSizeBytes = 10 * 1024 * 1024;

@Component({
  selector: "app-job-detail",
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="panel detail-panel">
      @if (job) {
        <span class="status">{{ jobStatusLabel(job.status) }}</span>
        <div class="detail-heading">
          <h2>{{ job.title }}</h2>

          @if (canManage) {
            <div class="detail-actions">
              <button class="ghost" type="button" [disabled]="loading" (click)="editJob.emit(job)">Editar</button>
              <button class="danger" type="button" [disabled]="loading || job.status === 'CANCELLED'" (click)="cancelJob.emit(job)">
                Cancelar vaga
              </button>
            </div>
          }
        </div>

        <p class="meta-line">{{ job.department }} - {{ job.location }}</p>
        <p>{{ job.description }}</p>
        <h3>Requisitos</h3>
        <p>{{ job.requirements }}</p>

        @if (canApply) {
          <form class="apply-box" [formGroup]="form" (ngSubmit)="submit()">
            <h3>Candidatura</h3>
            <label>
              Motivação
              <textarea formControlName="motivation"></textarea>
            </label>
            @if (form.controls.motivation.invalid && form.controls.motivation.touched) {
              <small>Informe uma motivação entre 10 e 2000 caracteres.</small>
            }

            @if (requireResumeOnApply) {
              <label class="file-picker apply-file-picker">
                Currículo em PDF
                <input type="file" accept="application/pdf,.pdf" [disabled]="loading" (change)="selectResumeFile($event)" />
                <span>{{ selectedResumeFile?.name ?? "Selecionar currículo" }}</span>
              </label>

              @if (resumeMessage) {
                <small>{{ resumeMessage }}</small>
              }
            }

            <button [disabled]="form.invalid || loading">Candidatar-se</button>
          </form>
        }
      } @else {
        <p>Nenhuma vaga aberta encontrada.</p>
      }
    </div>
  `,
})
export class JobDetailComponent {
  @Input() job: JobOpening | null = null;
  @Input() canApply = false;
  @Input() canManage = false;
  @Input() requireResumeOnApply = false;
  @Input() loading = false;
  @Output() applyToJob = new EventEmitter<JobApplicationSubmit>();
  @Output() editJob = new EventEmitter<JobOpening>();
  @Output() cancelJob = new EventEmitter<JobOpening>();

  selectedResumeFile: File | null = null;
  resumeMessage = "";

  readonly form = this.formBuilder.nonNullable.group({
    motivation: [
      "Tenho interesse na vaga e aderência aos requisitos.",
      [Validators.required, Validators.minLength(10), Validators.maxLength(2000)],
    ],
  });

  constructor(private readonly formBuilder: FormBuilder) {}

  protected readonly jobStatusLabel = jobStatusLabel;

  selectResumeFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.resumeMessage = "";

    if (!file) {
      this.selectedResumeFile = null;
      return;
    }

    if (file.type !== "application/pdf" && !file.name.toLowerCase().endsWith(".pdf")) {
      this.selectedResumeFile = null;
      this.resumeMessage = "Selecione um arquivo PDF.";
      input.value = "";
      return;
    }

    if (file.size > maxResumeSizeBytes) {
      this.selectedResumeFile = null;
      this.resumeMessage = "O currículo deve ter no máximo 10MB.";
      input.value = "";
      return;
    }

    this.selectedResumeFile = file;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.requireResumeOnApply && !this.selectedResumeFile) {
      this.resumeMessage = "Anexe seu currículo em PDF para se candidatar.";
      return;
    }

    this.applyToJob.emit({
      motivation: this.form.getRawValue().motivation.trim(),
      resumeFile: this.selectedResumeFile,
    });
  }
}
