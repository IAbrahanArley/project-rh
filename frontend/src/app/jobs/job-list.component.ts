import { Component, EventEmitter, Input, OnChanges, Output } from "@angular/core";
import { FormBuilder, ReactiveFormsModule } from "@angular/forms";
import { jobStatusLabel } from "../core/display-labels";
import { JobOpening, JobSearchFilters, JobStatus } from "../core/models";

@Component({
  selector: "app-job-list",
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="panel jobs-panel">
      <div class="panel-header">
        <h2>Vagas disponíveis</h2>
        @if (loading) {
          <span>Atualizando...</span>
        }
      </div>

      <form class="job-search-form" [formGroup]="searchForm" (ngSubmit)="submitSearch()">
        <label>
          Buscar
          <input formControlName="query" placeholder="Cargo, descrição ou requisito" />
        </label>

        <label>
          Departamento
          <input formControlName="department" placeholder="Ex: Tecnologia" />
        </label>

        <label>
          Localidade
          <input formControlName="location" placeholder="Ex: Remoto" />
        </label>

        <div class="form-actions search-actions">
          <button type="submit" [disabled]="loading">Filtrar</button>
          <button class="ghost" type="button" [disabled]="loading" (click)="clearSearch()">Limpar</button>
        </div>
      </form>

      @if (canFilterStatus) {
        <div class="segmented-control" aria-label="Filtro de status da vaga">
          <button
            class="ghost"
            type="button"
            [class.active]="selectedStatus === null"
            (click)="statusChange.emit(null)"
          >
            Todas
          </button>

          @for (option of statusOptions; track option.value) {
            <button
              class="ghost"
              type="button"
              [class.active]="selectedStatus === option.value"
              (click)="statusChange.emit(option.value)"
            >
              {{ option.label }}
            </button>
          }
        </div>
      }

      <div class="job-list">
        @for (job of jobs; track job.id) {
          <button
            type="button"
            class="job-item"
            [class.active]="job.id === selectedJobId"
            (click)="selectJob.emit(job.id)"
          >
            <strong>{{ job.title }}</strong>
            <span>{{ job.department }} - {{ job.location }}</span>
            <span class="status mini-status">{{ jobStatusLabel(job.status) }}</span>
          </button>
        } @empty {
          <p>Nenhuma vaga encontrada.</p>
        }
      </div>

      @if (totalPages > 1) {
        <div class="pagination">
          <button class="ghost" type="button" [disabled]="loading || first" (click)="pageChange.emit(page - 1)">
            Anterior
          </button>
          <span>Página {{ page + 1 }} de {{ totalPages }}</span>
          <button class="ghost" type="button" [disabled]="loading || last" (click)="pageChange.emit(page + 1)">
            Próxima
          </button>
        </div>
      }
    </div>
  `,
})
export class JobListComponent implements OnChanges {
  @Input({ required: true }) jobs: JobOpening[] = [];
  @Input({ required: true }) filters: JobSearchFilters = { query: "", department: "", location: "" };
  @Input() selectedJobId: string | null = null;
  @Input() selectedStatus: JobStatus | null = "OPEN";
  @Input() page = 0;
  @Input() totalPages = 0;
  @Input() first = true;
  @Input() last = true;
  @Input() canFilterStatus = false;
  @Input() loading = false;
  @Output() selectJob = new EventEmitter<string>();
  @Output() statusChange = new EventEmitter<JobStatus | null>();
  @Output() filtersChange = new EventEmitter<JobSearchFilters>();
  @Output() pageChange = new EventEmitter<number>();

  readonly searchForm = this.formBuilder.nonNullable.group({
    query: [""],
    department: [""],
    location: [""],
  });

  readonly statusOptions: Array<{ label: string; value: JobStatus }> = [
    { label: "Abertas", value: "OPEN" },
    { label: "Fechadas", value: "CLOSED" },
    { label: "Canceladas", value: "CANCELLED" },
  ];

  protected readonly jobStatusLabel = jobStatusLabel;

  constructor(private readonly formBuilder: FormBuilder) {}

  ngOnChanges(): void {
    const current = this.searchForm.getRawValue();

    if (
      current.query !== this.filters.query ||
      current.department !== this.filters.department ||
      current.location !== this.filters.location
    ) {
      this.searchForm.setValue(this.filters, { emitEvent: false });
    }
  }

  submitSearch(): void {
    const rawValue = this.searchForm.getRawValue();
    this.filtersChange.emit({
      query: rawValue.query.trim(),
      department: rawValue.department.trim(),
      location: rawValue.location.trim(),
    });
  }

  clearSearch(): void {
    this.searchForm.setValue({ query: "", department: "", location: "" });
    this.filtersChange.emit({ query: "", department: "", location: "" });
  }
}
