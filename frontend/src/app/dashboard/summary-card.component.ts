import { Component, Input } from "@angular/core";

@Component({
  selector: "app-summary-card",
  standalone: true,
  template: `
    <article class="summary">
      <span>{{ label }}</span>
      <strong>{{ value }}</strong>
    </article>
  `,
})
export class SummaryCardComponent {
  @Input({ required: true }) label = "";
  @Input({ required: true }) value = 0;
}
