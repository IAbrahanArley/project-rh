import { Component, computed } from "@angular/core";
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";
import { roleLabel } from "../core/display-labels";
import { AuthService } from "../core/auth.service";

@Component({
  selector: "app-candidate-shell",
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <main class="candidate-shell">
      @if (session()) {
        <aside class="candidate-sidebar">
          <div>
            <span class="eyebrow">Portal RH</span>
            <h1>Área do candidato</h1>
            <p>{{ session()!.user.fullName }}</p>
            <span>{{ roleLabel(session()!.user.role) }}</span>
          </div>

          <nav class="candidate-nav" aria-label="Navegação do candidato">
            <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Painel</a>
            <a routerLink="/vagas" routerLinkActive="active">Vagas</a>
            <a routerLink="/candidaturas" routerLinkActive="active">Candidaturas</a>
          </nav>

          <button class="ghost" type="button" (click)="logout()">Sair</button>
        </aside>

        <section class="candidate-main">
          <router-outlet />
        </section>
      }
    </main>
  `,
})
export class CandidateShellComponent {
  readonly session = computed(() => this.authService.session());

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  protected readonly roleLabel = roleLabel;

  logout(): void {
    this.authService.signOut();
    void this.router.navigateByUrl("/login");
  }
}
