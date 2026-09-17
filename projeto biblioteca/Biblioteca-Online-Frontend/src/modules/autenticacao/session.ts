"use client";

import type { TokenResponse } from "./dto/login";

type Session = { token: string; expiresAt: number };

const key = "biblioteca.session";
const listeners = new Set<() => void>();
let cached: Session | null | undefined;

export function read(): Session | null {
  if (cached !== undefined) return cached;
  
  try {
    const value = JSON.parse(sessionStorage.getItem(key) || "null");
    cached =
      value &&
      typeof value.token === "string" &&
      typeof value.expiresAt === "number" &&
      value.expiresAt > Date.now()
        ? value
        : null;
  } catch {
    cached = null;
  }
  return cached ?? null;
}
export function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}
function publish(session: Session | null) {
  cached = session;
  try {
    if (session) sessionStorage.setItem(key, JSON.stringify(session));
    else sessionStorage.removeItem(key);
  } catch {
    /* Mantém a sessão em memória se o armazenamento estiver indisponível. */
  }
  listeners.forEach((listener) => listener());
}
export function iniciarSessao(response: TokenResponse) {
  publish({
    token: response.accessToken,
    expiresAt: Date.now() + response.expiresIn * 1000,
  });
}
export function encerrarSessao() {
  publish(null);
}
