import assert from "node:assert/strict";
import { afterEach, test } from "node:test";
import { criarLogin } from "../src/modules/autenticacao/dto/login";
import { criarUsuario } from "../src/modules/usuario/dto/usuario";
import {
  ConsultaLivros,
  linkSeguro,
} from "../src/modules/biblioteca/dto/livro";
import { autenticar } from "../src/modules/autenticacao/services/autenticacao-api";
import {
  cadastrarUsuario,
  listarUsuarios,
} from "../src/modules/usuario/services/usuario-api";
import {
  buscarLivros,
  buscarLivro,
} from "../src/modules/biblioteca/services/biblioteca-api";
import { api, ApiError } from "../src/shared/http/api";

const originalFetch = globalThis.fetch;
afterEach(() => {
  globalThis.fetch = originalFetch;
});
function form(values: Record<string, string>) {
  const data = new FormData();
  Object.entries(values).forEach(([key, value]) => data.set(key, value));
  return data;
}
const cadastro = {
  nome: " Leitora Teste ",
  cpf: "123.456.789-01",
  senha: " senha123 ",
  cep: "01001-000",
  numero: " 42 ",
  complemento: "",
};
function intercept(body: unknown = {}) {
  const calls: { url: string; options: RequestInit }[] = [];
  globalThis.fetch = async (input, options = {}) => {
    calls.push({ url: String(input), options });
    return Response.json(body);
  };
  return calls;
}
test("login aceita máscara de CPF sem modificar a senha", () => {
  assert.deepEqual(criarLogin(form(cadastro)), {
    cpf: "12345678901",
    senha: " senha123 ",
  });
});
test("cadastro produz endereço aninhado e apenas os campos do backend", () => {
  assert.deepEqual(criarUsuario(form(cadastro)), {
    nome: "Leitora Teste",
    cpf: "12345678901",
    senha: " senha123 ",
    endereco: { cep: "01001000", numero: "42", complemento: "" },
  });
});
test("validação rejeita CPF com letras, CEP incompleto, senha curta e campos vazios", () => {
  for (const override of [
    { cpf: "1234567890a" },
    { cep: "123" },
    { senha: "1234567" },
    { nome: " " },
    { numero: " " },
  ]) {
    assert.throws(() => criarUsuario(form({ ...cadastro, ...override })));
  }
  assert.throws(() => criarLogin(form({ cpf: cadastro.cpf, senha: "   " })));
});
test("consulta usa páginas a partir de zero e preserva caracteres da busca", () => {
  const params = new URLSearchParams(
    new ConsultaLivros(" ficção & ciência ", 2).toSearchParams(),
  );
  assert.equal(params.get("termo"), "ficção & ciência");
  assert.equal(params.get("page"), "2");
  assert.equal(params.get("size"), "12");
  assert.throws(() => new ConsultaLivros(" "));
  assert.throws(() => new ConsultaLivros("livro", -1));
  assert.throws(() => new ConsultaLivros("livro", 1.5));
});
test("links externos bloqueiam execução de scripts e conteúdo embutido", () => {
  assert.equal(linkSeguro("javascript:alert(1)"), undefined);
  assert.equal(linkSeguro("data:text/html,<script>"), undefined);
  assert.equal(
    linkSeguro("http://books.google.com/test"),
    "https://books.google.com/test",
  );
});
test("login envia POST público e consome TokenResponse", async () => {
  const token = {
    accessToken: "token-teste",
    tokenType: "Bearer",
    expiresIn: 3600,
  };
  const calls = intercept(token);
  assert.deepEqual(await autenticar(criarLogin(form(cadastro))), token);
  assert.equal(calls[0].url, "/api/auth/login");
  assert.equal(calls[0].options.method, "POST");
  assert.equal(
    new Headers(calls[0].options.headers).get("Authorization"),
    null,
  );
  assert.equal(JSON.parse(String(calls[0].options.body)).cpf, "12345678901");
});
test("cadastro envia o contrato e listagem envia Bearer", async () => {
  const calls = intercept([]);
  const request = criarUsuario(form(cadastro));
  await cadastrarUsuario(request);
  await listarUsuarios("token-teste", new AbortController().signal);
  assert.equal(calls[0].url, "/api/usuarios");
  assert.equal(calls[0].options.method, "POST");
  assert.deepEqual(JSON.parse(String(calls[0].options.body)), request);
  assert.equal(
    new Headers(calls[1].options.headers).get("Authorization"),
    "Bearer token-teste",
  );
});
test("catálogo e detalhes usam endpoints, paginação e token do backend", async () => {
  const calls = intercept({ totalItems: 0, items: null });
  const signal = new AbortController().signal;
  assert.deepEqual(
    await buscarLivros(new ConsultaLivros("livros", 1), "token-teste", signal),
    { totalItems: 0, items: null },
  );
  await buscarLivro("id/com espaço", "token-teste", signal);
  assert.equal(calls[0].url, "/api/livros?termo=livros&page=1&size=12");
  assert.equal(calls[1].url, "/api/livros/id%2Fcom%20espa%C3%A7o");
  assert.equal(calls[0].options.signal, signal);
  assert.equal(
    new Headers(calls[0].options.headers).get("Authorization"),
    "Bearer token-teste",
  );
});
test("401 sem JSON conserva status para expirar a sessão", async () => {
  globalThis.fetch = async () => new Response(null, { status: 401 });
  await assert.rejects(
    api("/livros"),
    (error: unknown) => error instanceof ApiError && error.status === 401,
  );
});
test("ProblemDetail exibe a mensagem de indisponibilidade do catálogo", async () => {
  globalThis.fetch = async () =>
    Response.json({ detail: "Google Books indisponível." }, { status: 502 });
  await assert.rejects(api("/livros"), /Google Books indisponível/);
});
test("falha de rede e cancelamento são tratados separadamente", async () => {
  globalThis.fetch = async () => {
    throw new TypeError("Failed to fetch");
  };
  await assert.rejects(api("/livros"), /conectar ao servidor/);
  globalThis.fetch = async () => {
    throw new DOMException("Aborted", "AbortError");
  };
  await assert.rejects(api("/livros"), { name: "AbortError" });
});
