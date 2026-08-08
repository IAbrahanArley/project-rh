import { Component, EventEmitter, Input, Output } from "@angular/core";
import { AuthenticatedUser } from "../core/models";
import { roleLabel } from "../core/display-labels";

@Component({
  selector: "app-topbar",
  standalone: true,
  template: `
    <header class="topbar">
      <div>
        <span class="eyebrow">Portal RH</span>
        <h1>{{ user.role === "ADMIN" ? "Gestao de vagas" : "Vagas internas" }}</h1>
        <p>{{ user.role === "ADMIN" ? "Acompanhe vagas, candidaturas e notificacoes do processo." : "Consulte oportunidades e acompanhe suas candidaturas." }}</p>
      </div>

      <div class="user-box">
        <strong>{{ user.fullName }}</strong>
        <span>{{ roleLabel(user.role) }}</span>
        <button class="ghost" type="button" (click)="logout.emit()">Sair</button>
      </div>
    </header>
  `,
})
export class TopbarComponent {
  @Input({ required: true }) user!: AuthenticatedUser;
  @Output() logout = new EventEmitter<void>();

  protected readonly roleLabel = roleLabel;
}
