import { Component, EventEmitter, Input, Output } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { jobStatusLabel } from "../core/display-labels";
import { JobOpening } from "../core/models";

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
            <textarea formControlName="motivation"></textarea>
            @if (form.controls.motivation.invalid && form.controls.motivation.touched) {
              <small>Informe uma motivação entre 10 e 2000 caracteres.</small>
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
  @Input() loading = false;
  @Output() applyToJob = new EventEmitter<string>();
  @Output() editJob = new EventEmitter<JobOpening>();
  @Output() cancelJob = new EventEmitter<JobOpening>();

  readonly form = this.formBuilder.nonNullable.group({
    motivation: [
      "Tenho interesse na vaga e aderência aos requisitos.",
      [Validators.required, Validators.minLength(10), Validators.maxLength(2000)],
    ],
  });

  constructor(private readonly formBuilder: FormBuilder) {}

  protected readonly jobStatusLabel = jobStatusLabel;

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.applyToJob.emit(this.form.getRawValue().motivation.trim());
  }
}
