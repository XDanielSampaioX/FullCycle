import { api } from "@/shared/http/api";
import type { ConsultaLivros, GoogleBooksResponse, Livro } from "../dto/livro";
export function buscarLivros(
  consulta: ConsultaLivros,
  token: string,
  signal: AbortSignal,
) {
  return api<GoogleBooksResponse>(
    `/livros?${consulta.toSearchParams()}`,
    { signal },
    token,
  );
}
export function buscarLivro(id: string, token: string, signal: AbortSignal) {
  return api<Livro>(`/livros/${encodeURIComponent(id)}`, { signal }, token);
}
