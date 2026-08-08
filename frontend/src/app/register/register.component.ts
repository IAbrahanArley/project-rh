import { Component } from "@angular/core";
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { finalize } from "rxjs";
import { AuthService } from "../core/auth.service";
import { RecruitmentApiService } from "../core/recruitment-api.service";

type RegisterControlName = "username" | "fullName" | "email" | "password" | "confirmPassword";

@Component({
  selector: "app-register",
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="login-shell auth-shell">
      <section class="login-panel register-panel">
        <div>
          <span class="eyebrow">Conta de candidato</span>
          <h1>Crie seu acesso</h1>
          <p>Cadastre uma conta para acompanhar vagas e suas candidaturas no portal.</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()" class="login-form register-form">
          <label>
            Nome completo
            <input formControlName="fullName" autocomplete="name" maxlength="120" />
            @if (isInvalid("fullName")) {
              <small>Informe seu nome completo.</small>
            }
          </label>

          <label>
            E-mail
            <input formControlName="email" type="email" autocomplete="email" maxlength="160" />
            @if (isInvalid("email")) {
              <small>Informe um e-mail válido.</small>
            }
          </label>

          <label>
            Usuário
            <input formControlName="username" autocomplete="username" maxlength="80" />
            @if (isInvalid("username")) {
              <small>Use 3 a 80 caracteres: letras, números, ponto, underline ou hífen.</small>
            }
          </label>

          <label>
            Senha
            <span class="password-field">
              <input
                formControlName="password"
                [type]="showPassword ? 'text' : 'password'"
                autocomplete="new-password"
                maxlength="72"
              />
              <button
                class="password-toggle"
                type="button"
                [attr.aria-label]="showPassword ? 'Ocultar senha' : 'Mostrar senha'"
                [attr.title]="showPassword ? 'Ocultar senha' : 'Mostrar senha'"
                (click)="togglePasswordVisibility()"
              >
                <span class="eye-icon" [class.hidden]="showPassword"></span>
              </button>
            </span>
            @if (isInvalid("password")) {
              <small>A senha precisa ter no mínimo 8 caracteres, com letras e números.</small>
            }
          </label>

          <label>
            Confirmar senha
            <span class="password-field">
              <input
                formControlName="confirmPassword"
                [type]="showConfirmPassword ? 'text' : 'password'"
                autocomplete="new-password"
                maxlength="72"
              />
              <button
                class="password-toggle"
                type="button"
                [attr.aria-label]="showConfirmPassword ? 'Ocultar confirmação de senha' : 'Mostrar confirmação de senha'"
                [attr.title]="showConfirmPassword ? 'Ocultar confirmação de senha' : 'Mostrar confirmação de senha'"
                (click)="toggleConfirmPasswordVisibility()"
              >
                <span class="eye-icon" [class.hidden]="showConfirmPassword"></span>
              </button>
            </span>
            @if (isInvalid("confirmPassword") || passwordsDoNotMatch) {
              <small>As senhas precisam ser iguais.</small>
            }
          </label>

          <button [disabled]="form.invalid || loading">{{ loading ? "Criando conta..." : "Criar conta" }}</button>
        </form>

        @if (errorMessage) {
          <p class="alert">{{ errorMessage }}</p>
        }

        <p class="auth-switch">
          Já tem conta?
          <a routerLink="/login">Entrar no portal</a>
        </p>
      </section>
    </main>
  `,
})
export class RegisterComponent {
  readonly form = this.formBuilder.nonNullable.group(
    {
      username: [
        "",
        [Validators.required, Validators.minLength(3), Validators.maxLength(80), Validators.pattern(/^[A-Za-z0-9._-]+$/)],
      ],
      fullName: ["", [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      email: ["", [Validators.required, Validators.email, Validators.maxLength(160)]],
      password: [
        "",
        [Validators.required, Validators.minLength(8), Validators.maxLength(72), Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d).+$/)],
      ],
      confirmPassword: ["", [Validators.required]],
    },
    { validators: this.passwordsMatch },
  );

  loading = false;
  errorMessage = "";
  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly api: RecruitmentApiService,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  get passwordsDoNotMatch(): boolean {
    return this.form.hasError("passwordsDoNotMatch") && this.form.controls.confirmPassword.touched;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = "";
    const { confirmPassword: _confirmPassword, ...request } = this.form.getRawValue();

    this.api
      .registerCandidate({
        username: request.username.trim().toLowerCase(),
        fullName: request.fullName.trim().replace(/\s+/g, " "),
        email: request.email.trim().toLowerCase(),
        password: request.password,
      })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (response) => {
          this.authService.signIn(response);
          void this.router.navigateByUrl("/");
        },
        error: () => {
          this.errorMessage = "Não foi possível criar a conta. Confira os dados ou tente outro usuário/e-mail.";
        },
      });
  }

  isInvalid(controlName: RegisterControlName): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  private passwordsMatch(control: AbstractControl): ValidationErrors | null {
    const password = control.get("password")?.value;
    const confirmation = control.get("confirmPassword")?.value;

    return password && confirmation && password !== confirmation ? { passwordsDoNotMatch: true } : null;
  }
}
