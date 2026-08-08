import { Component, Input } from "@angular/core";
import { applicationStatusLabel } from "../core/display-labels";
import { JobApplication } from "../core/models";

@Component({
  selector: "app-application-list",
  standalone: true,
  template: `
    <div class="panel">
      <div class="panel-header">
        <h2>Candidaturas</h2>
      </div>

      <div class="stack">
        @for (application of applications; track application.id) {
          <article class="compact-card">
            <strong>{{ application.jobTitle }}</strong>
            <span>{{ applicationStatusLabel(application.status) }}</span>
            @if (application.feedback) {
              <p>{{ application.feedback }}</p>
            }
          </article>
        } @empty {
          <p>Nenhuma candidatura encontrada.</p>
        }
      </div>
    </div>
  `,
})
export class ApplicationListComponent {
  @Input({ required: true }) applications: JobApplication[] = [];

  protected readonly applicationStatusLabel = applicationStatusLabel;
}
