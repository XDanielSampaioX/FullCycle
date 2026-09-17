import { api } from "@/shared/http/api";
import type { UsuarioRequest, UsuarioResponse } from "../dto/usuario";
export function cadastrarUsuario(request: UsuarioRequest) {
  return api<UsuarioResponse>("/usuarios", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
export function listarUsuarios(token: string, signal: AbortSignal) {
  return api<UsuarioResponse[]>("/usuarios", { signal }, token);
}
