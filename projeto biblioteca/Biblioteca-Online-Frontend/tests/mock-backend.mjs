// Servidor isolado para verificações manuais. Não acessa o banco nem serviços externos.
import { createServer } from "node:http";
const users = [{ id: "usuario-teste", nome: "Pessoa de teste", cpf: "12345678901", endereco: { localidade: "São Paulo", uf: "SP" } }];
const books = Array.from({ length: 13 }, (_, index) => ({
  id: "livro-" + index,
  volumeInfo: { title: "Livro de teste " + (index + 1), authors: ["Autoria de teste"], categories: ["Literatura"], description: "Descrição de teste para verificar a apresentação de um livro.", pageCount: 120, language: "pt", publisher: "Editora de teste", publishedDate: "2026", imageLinks: null },
  accessInfo: { webReaderLink: null, pdf: null, epub: null }, saleInfo: null,
}));
createServer(async (req, res) => {
  const url = new URL(req.url, "http://localhost");
  const reply = (status, value) => { res.writeHead(status, { "Content-Type": "application/json" }); res.end(JSON.stringify(value)); };
  let body = "";
  for await (const part of req) body += part;
  if (url.pathname === "/api/auth/login") return reply(200, { accessToken: "token-isolado", tokenType: "Bearer", expiresIn: 600 });
  if (url.pathname === "/api/usuarios" && req.method === "POST") { const user = { ...JSON.parse(body), id: String(users.length) }; delete user.senha; users.push(user); return reply(201, user); }
  if (req.headers.authorization !== "Bearer token-isolado") return reply(401, {});
  if (url.pathname === "/api/usuarios") return reply(200, users);
  if (url.pathname === "/api/livros") {
    if (url.searchParams.get("termo") === "erro") return reply(502, { detail: "Falha de teste do serviço de livros." });
    if (url.searchParams.get("termo") === "vazio") return reply(200, { totalItems: 0, items: null });
    const page = Number(url.searchParams.get("page")); const size = Number(url.searchParams.get("size"));
    return reply(200, { totalItems: books.length, items: books.slice(page * size, (page + 1) * size) });
  }
  const book = books.find(book => url.pathname === "/api/livros/" + book.id);
  return book ? reply(200, book) : reply(404, {});
}).listen(18080, "127.0.0.1", () => console.log("Backend de teste isolado: http://127.0.0.1:18080"));

