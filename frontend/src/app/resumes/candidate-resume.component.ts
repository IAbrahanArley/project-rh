import { Component, Input, OnInit, signal } from "@angular/core";
import { switchMap } from "rxjs";
import { Resume } from "../core/models";
import { RecruitmentApiService } from "../core/recruitment-api.service";

const maxResumeSizeBytes = 10 * 1024 * 1024;

@Component({
  selector: "app-candidate-resume",
  standalone: true,
  template: `
    <div class="panel resume-panel">
      <div class="panel-header">
        <div>
          <h2>Currículo</h2>
          @if (resume()) {
            <span>{{ resume()!.fileName }} - {{ formatSize(resume()!.sizeBytes) }}</span>
          } @else {
            <span>Nenhum PDF enviado</span>
          }
        </div>
        @if (loading()) {
          <span>Processando...</span>
        }
      </div>

      <div class="resume-actions">
        <label class="file-picker">
          <input type="file" accept="application/pdf,.pdf" [disabled]="loading()" (change)="selectFile($event)" />
          <span>{{ selectedFile()?.name ?? "Selecionar PDF" }}</span>
        </label>

        <button [disabled]="!selectedFile() || loading()" (click)="uploadSelectedFile()">
          {{ resume() ? "Substituir currículo" : "Enviar currículo" }}
        </button>

        <button class="ghost" [disabled]="!resume() || loading()" (click)="openResume()">Abrir PDF</button>
      </div>

      @if (message()) {
        <p class="feedback">{{ message() }}</p>
      }
    </div>
  `,
})
export class CandidateResumeComponent implements OnInit {
  @Input({ required: true }) token = "";

  readonly resume = signal<Resume | null>(null);
  readonly selectedFile = signal<File | null>(null);
  readonly loading = signal(false);
  readonly message = signal("");

  constructor(private readonly api: RecruitmentApiService) {}

  ngOnInit(): void {
    this.loadResume();
  }

  selectFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.message.set("");

    if (!file) {
      this.selectedFile.set(null);
      return;
    }

    if (file.type !== "application/pdf" && !file.name.toLowerCase().endsWith(".pdf")) {
      this.selectedFile.set(null);
      this.message.set("Selecione um arquivo PDF.");
      input.value = "";
      return;
    }

    if (file.size > maxResumeSizeBytes) {
      this.selectedFile.set(null);
      this.message.set("O currículo deve ter no máximo 10MB.");
      input.value = "";
      return;
    }

    this.selectedFile.set(file);
  }

  uploadSelectedFile(): void {
    const file = this.selectedFile();

    if (!file) {
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api
      .createResumeUploadUrl(this.token, {
        fileName: file.name,
        contentType: "application/pdf",
        sizeBytes: file.size,
      })
      .pipe(
        switchMap((upload) =>
          this.api.uploadResumeFile(upload.uploadUrl, file).pipe(
            switchMap(() =>
              this.api.completeResumeUpload(this.token, {
                storageKey: upload.storageKey,
                fileName: file.name,
                contentType: upload.requiredContentType,
                sizeBytes: file.size,
              }),
            ),
          ),
        ),
      )
      .subscribe({
        next: (resume) => {
          this.resume.set(resume);
          this.selectedFile.set(null);
          this.message.set("Currículo enviado com sucesso.");
          this.loading.set(false);
        },
        error: () => {
          this.message.set("Não foi possível enviar o currículo. Verifique o PDF e tente novamente.");
          this.loading.set(false);
        },
      });
  }

  openResume(): void {
    if (!this.resume()) {
      return;
    }

    this.loading.set(true);
    this.message.set("");

    this.api.createMyResumeDownloadUrl(this.token).subscribe({
      next: (response) => {
        window.open(response.downloadUrl, "_blank", "noopener,noreferrer");
        this.loading.set(false);
      },
      error: () => {
        this.message.set("Não foi possível abrir o currículo.");
        this.loading.set(false);
      },
    });
  }

  formatSize(sizeBytes: number): string {
    return `${(sizeBytes / 1024 / 1024).toFixed(2)} MB`;
  }

  private loadResume(): void {
    if (!this.token) {
      return;
    }

    this.loading.set(true);

    this.api.getMyResume(this.token).subscribe({
      next: (resume) => {
        this.resume.set(resume);
        this.loading.set(false);
      },
      error: () => {
        this.message.set("Não foi possível carregar o currículo.");
        this.loading.set(false);
      },
    });
  }
}
