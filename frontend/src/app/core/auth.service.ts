import { Injectable, signal } from "@angular/core";
import { AuthResponse, AuthenticatedUser } from "./models";

export interface Session {
  token: string;
  user: AuthenticatedUser;
}

const storageKey = "rh.session";

@Injectable({ providedIn: "root" })
export class AuthService {
  readonly session = signal<Session | null>(this.restoreSession());

  signIn(response: AuthResponse): void {
    const session = {
      token: response.accessToken,
      user: response.user,
    };

    localStorage.setItem(storageKey, JSON.stringify(session));
    this.session.set(session);
  }

  signOut(): void {
    localStorage.removeItem(storageKey);
    this.session.set(null);
  }

  private restoreSession(): Session | null {
    const rawSession = localStorage.getItem(storageKey);

    if (!rawSession) {
      return null;
    }

    try {
      const session = JSON.parse(rawSession) as Partial<Session>;

      if (!this.isValidSession(session)) {
        localStorage.removeItem(storageKey);
        return null;
      }

      return session;
    } catch {
      localStorage.removeItem(storageKey);
      return null;
    }
  }

  private isValidSession(session: Partial<Session>): session is Session {
    return (
      typeof session.token === "string" &&
      session.token.length > 0 &&
      !!session.user &&
      typeof session.user.username === "string" &&
      typeof session.user.fullName === "string" &&
      (session.user.role === "ADMIN" || session.user.role === "CANDIDATE")
    );
  }
}
