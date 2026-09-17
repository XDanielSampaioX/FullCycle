import { digitos, obrigatorio, texto } from "@/shared/domain/campos";
import {
  criarLogin,
  type LoginRequest,
} from "@/modules/autenticacao/dto/login";
export interface EnderecoRequest {
  cep: string;
  numero: string;
  complemento: string;
}
export interface UsuarioRequest extends LoginRequest {
  nome: string;
  endereco: EnderecoRequest;
}
export interface EnderecoResponse extends EnderecoRequest {
  logradouro: string | null;
  rua: string | null;
  bairro: string | null;
  localidade: string | null;
  uf: string | null;
  estado: string | null;
}
export interface UsuarioResponse {
  id: string;
  nome: string;
  cpf: string;
  endereco: EnderecoResponse | null;
}
export function criarUsuario(form: FormData): UsuarioRequest {
  const login = criarLogin(form);
  if (!/^.{8,}$/.test(login.senha))
    throw new Error("A senha deve conter pelo menos 8 caracteres.");
  return {
    ...login,
    nome: obrigatorio(texto(form, "nome"), "Nome"),
    endereco: {
      cep: digitos(texto(form, "cep"), 8, "CEP"),
      numero: obrigatorio(texto(form, "numero"), "Número"),
      complemento: texto(form, "complemento"),
    },
  };
}
