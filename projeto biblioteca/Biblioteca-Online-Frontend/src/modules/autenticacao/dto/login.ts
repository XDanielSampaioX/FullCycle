import { digitos, obrigatorio, texto } from "@/shared/domain/campos";
export interface LoginRequest {
  cpf: string;
  senha: string;
}
export interface TokenResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}
export function criarLogin(form: FormData): LoginRequest {
  return {
    cpf: digitos(texto(form, "cpf"), 11, "CPF"),
    senha: obrigatorio(String(form.get("senha") ?? ""), "Senha"),
  };
}
