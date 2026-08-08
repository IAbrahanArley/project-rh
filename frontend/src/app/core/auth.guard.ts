import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "./auth.service";

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.session()) {
    return true;
  }

  return router.createUrlTree(["/login"]);
};

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const session = authService.session();

  if (!session) {
    return router.createUrlTree(["/login"]);
  }

  if (session.user.role === "ADMIN") {
    return true;
  }

  return router.createUrlTree(["/"]);
};

export const candidateGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const session = authService.session();

  if (!session) {
    return router.createUrlTree(["/login"]);
  }

  if (session.user.role === "CANDIDATE") {
    return true;
  }

  return router.createUrlTree(["/admin"]);
};

export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.session()) {
    return true;
  }

  return router.createUrlTree(["/"]);
};
