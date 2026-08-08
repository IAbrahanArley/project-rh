import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { jobStatusLabel } from "../core/display-labels";
import { JobOpening, JobOpeningRequest, JobStatus } from "../core/models";

export type JobFormMode = "create" | "edit";

@Component({
  selector: "app-job-form",
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="panel">
      <div class="panel-header">
        <h2>{{ mode === "edit" ? "Editar vaga" : "Nova vaga" }}</h2>
      </div>

      <form class="job-form" [formGroup]="form" (ngSubmit)="submit()">
        <label>
          Titulo
          <input formControlName="title" placeholder="Analista Java Pleno" />
          @if (isInvalid("title")) {
            <small>Informe um título com até 140 caracteres.</small>
          }
        </label>

        <label>
          Departamento
          <input formControlName="department" placeholder="Tecnologia" />
          @if (isInvalid("department")) {
            <small>Informe o departamento.</small>
          }
        </label>

        <label>
          Local
          <input formControlName="location" placeholder="Recife - PE" />
          @if (isInvalid("location")) {
            <small>Informe o local.</small>
          }
        </label>

        <label>
          Descrição
          <textarea formControlName="description" placeholder="Responsabilidades da vaga"></textarea>
          @if (isInvalid("description")) {
            <small>Informe uma descrição.</small>
          }
        </label>

        <label>
          Requisitos
          <textarea formControlName="requirements" placeholder="Java, Spring Boot, Angular"></textarea>
          @if (isInvalid("requirements")) {
            <small>Informe os requisitos.</small>
          }
        </label>

        @if (mode === "edit") {
          <label>
            Status
            <select formControlName="status">
              @for (status of statusOptions; track status) {
                <option [value]="status">{{ jobStatusLabel(status) }}</option>
              }
            </select>
          </label>
        }

        <div class="form-actions">
          <button [disabled]="form.invalid || loading">{{ mode === "edit" ? "Salvar alterações" : "Cadastrar vaga" }}</button>
          @if (mode === "edit") {
            <button class="ghost" type="button" [disabled]="loading" (click)="cancelEdit.emit()">Cancelar edição</button>
          }
        </div>
      </form>
    </div>
  `,
})
export class JobFormComponent implements OnChanges {
  @Input() loading = false;
  @Input() mode: JobFormMode = "create";
  @Input() job: JobOpening | null = null;
  @Output() saveJob = new EventEmitter<JobOpeningRequest>();
  @Output() cancelEdit = new EventEmitter<void>();

  readonly statusOptions: JobStatus[] = ["OPEN", "CLOSED", "CANCELLED"];
  protected readonly jobStatusLabel = jobStatusLabel;

  readonly form = this.formBuilder.nonNullable.group({
    title: ["", [Validators.required, Validators.maxLength(140)]],
    department: ["", [Validators.required, Validators.maxLength(80)]],
    location: ["", [Validators.required, Validators.maxLength(80)]],
    description: ["", [Validators.required, Validators.maxLength(4000)]],
    requirements: ["", [Validators.required, Validators.maxLength(4000)]],
    status: ["OPEN" as JobStatus, [Validators.required]],
  });

  constructor(private readonly formBuilder: FormBuilder) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes["job"] && !changes["mode"]) {
      return;
    }

    if (this.mode === "edit" && this.job) {
      this.form.setValue({
        title: this.job.title,
        department: this.job.department,
        location: this.job.location,
        description: this.job.description,
        requirements: this.job.requirements,
        status: this.job.status,
      });
      return;
    }

    this.form.reset({
      title: "",
      department: "",
      location: "",
      description: "",
      requirements: "",
      status: "OPEN",
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const rawValue = this.form.getRawValue();

    this.saveJob.emit({
      title: rawValue.title.trim(),
      department: rawValue.department.trim(),
      location: rawValue.location.trim(),
      description: rawValue.description.trim(),
      requirements: rawValue.requirements.trim(),
      status: rawValue.status,
    });

    if (this.mode === "create") {
      this.form.reset({
        title: "",
        department: "",
        location: "",
        description: "",
        requirements: "",
        status: "OPEN",
      });
    }
  }

  isInvalid(controlName: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }
}
