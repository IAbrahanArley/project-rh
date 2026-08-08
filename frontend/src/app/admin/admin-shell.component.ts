import { Component, computed } from "@angular/core";
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";
import { AuthService } from "../core/auth.service";
import { roleLabel } from "../core/display-labels";

@Component({
  selector: "app-admin-shell",
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <main class="candidate-shell">
      @if (session()) {
        <aside class="candidate-sidebar">
          <div>
            <span class="eyebrow">Portal RH</span>
            <h1>Área de gestão</h1>
            <p>{{ session()!.user.fullName }}</p>
            <span>{{ roleLabel(session()!.user.role) }}</span>
          </div>

          <nav class="candidate-nav" aria-label="Navegação da gestão">
            <a routerLink="/admin" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Painel</a>
            <a routerLink="/admin/vagas" routerLinkActive="active">Vagas</a>
            <a routerLink="/admin/vagas/nova" routerLinkActive="active">Nova vaga</a>
            <a routerLink="/admin/candidaturas" routerLinkActive="active">Candidaturas</a>
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
export class AdminShellComponent {
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
