import { Component } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { finalize } from "rxjs";
import { AuthService } from "../core/auth.service";
import { RecruitmentApiService } from "../core/recruitment-api.service";

@Component({
  selector: "app-login",
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="login-shell">
      <section class="login-panel">
        <div>
          <span class="eyebrow">Recrutamento interno</span>
          <h1>Portal RH</h1>
          <p>Acesse sua conta para consultar vagas, candidaturas e notificações.</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()" class="login-form">
          <label>
            Usuário
            <input formControlName="username" autocomplete="username" />
            @if (isInvalid("username")) {
              <small>Informe o usuário.</small>
            }
          </label>

          <label>
            Senha
            <input formControlName="password" type="password" autocomplete="current-password" />
            @if (isInvalid("password")) {
              <small>Informe a senha.</small>
            }
          </label>

          <button [disabled]="form.invalid || loading">{{ loading ? "Entrando..." : "Entrar" }}</button>
        </form>

        <p class="auth-switch">
          Ainda não tem conta?
          <a routerLink="/register">Criar conta de candidato</a>
        </p>

        @if (errorMessage) {
          <p class="alert">{{ errorMessage }}</p>
        }
      </section>
    </main>
  `,
})
export class LoginComponent {
  readonly form = this.formBuilder.nonNullable.group({
    username: ["", [Validators.required]],
    password: ["", [Validators.required]],
  });

  loading = false;
  errorMessage = "";

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly api: RecruitmentApiService,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = "";

    this.api
      .login(this.form.getRawValue())
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (response) => {
          this.authService.signIn(response);
          void this.router.navigateByUrl("/");
        },
        error: () => {
          this.errorMessage = "Usuário ou senha incorretos. Confira os dados e tente novamente.";
        },
      });
  }

  isInvalid(controlName: "username" | "password"): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }
}
