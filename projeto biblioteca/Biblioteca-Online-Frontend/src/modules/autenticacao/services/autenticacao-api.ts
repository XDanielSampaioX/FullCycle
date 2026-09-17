import { api } from "@/shared/http/api";
import type { LoginRequest, TokenResponse } from "../dto/login";

export function autenticar(request: LoginRequest) {
  return api<TokenResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
