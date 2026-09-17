"use client";
import { useCallback, useState, type FormEvent } from "react";
import Link from "next/link";
import { useConsulta } from "@/modules/autenticacao/hooks/use-consulta";
import { ConsultaStatus } from "@/shared/components/consulta-status";
import { Aviso, Campo } from "@/shared/components/ui";
import { ConsultaLivros } from "../dto/livro";
import { buscarLivros } from "../services/biblioteca-api";
import { Capa } from "./capa";
export function Catalogo() {
  const [consulta, setConsulta] = useState(() => new ConsultaLivros());
  const [error, setError] = useState("");
  const load = useCallback(
    (token: string, signal: AbortSignal) =>
      buscarLivros(consulta, token, signal),
    [consulta],
  );
  const query = useConsulta(load);
  function search(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    try {
      setConsulta(
        new ConsultaLivros(
          String(new FormData(event.currentTarget).get("termo") ?? ""),
        ),
      );
    } catch (error) {
      setError(error instanceof Error ? error.message : "Informe um termo.");
    }
  }
  const items = query.data?.items ?? [];
  return (
    <>
      <section className="catalog-hero">
        <div>
          <p className="eyebrow">SUA PRÓXIMA DESCOBERTA</p>
          <h1>
            Há sempre uma boa
            <br />
            história <em>à sua espera.</em>
          </h1>
          <p>Explore livros gratuitos e encontre sua próxima leitura.</p>
        </div>
        <div className="hero-mark" aria-hidden="true">
          ▤
        </div>
      </section>
      <form className="search panel" onSubmit={search}>
        <Campo
          label="O que você quer ler?"
          name="termo"
          placeholder="Busque por título, autor ou assunto"
          defaultValue={consulta.termo}
          required
        />
        <button className="button" type="submit">
          Buscar livros →
        </button>
      </form>
      {error && <Aviso erro>{error}</Aviso>}
      <div className="section-heading">
        <div>
          <p className="eyebrow">EXPLORE A BIBLIOTECA</p>
          <h2>Catálogo de livros</h2>
        </div>
        <span className="badge">Leituras gratuitas</span>
      </div>
      <ConsultaStatus {...query} />
      {query.data && (
        <>
          <p className="muted small">
            Resultados para “{consulta.termo}” · Página {consulta.page + 1}
          </p>
          {items.length === 0 ? (
            <Aviso>
              Nenhum livro encontrado nesta página. Tente outro termo ou volte à
              página anterior.
            </Aviso>
          ) : (
            <div className="book-grid">
              {items.map((livro) => (
                <Link
                  className="book-card"
                  key={livro.id}
                  href={`/catalogo/${encodeURIComponent(livro.id)}`}
                >
                  <Capa livro={livro} />
                  <div className="book-info">
                    <span className="book-category">
                      {livro.volumeInfo?.categories?.[0] || "Livro digital"}
                    </span>
                    <h3>{livro.volumeInfo?.title || "Sem título"}</h3>
                    <p>
                      {livro.volumeInfo?.authors?.join(", ") ||
                        "Autoria não informada"}
                    </p>
                    <span className="book-action">
                      Conhecer o livro <span>↗</span>
                    </span>
                  </div>
                </Link>
              ))}
            </div>
          )}
          <nav className="pagination" aria-label="Paginação do catálogo">
            <button
              className="button secondary"
              disabled={consulta.page === 0}
              onClick={() =>
                setConsulta(
                  new ConsultaLivros(consulta.termo, consulta.page - 1),
                )
              }
            >
              ← Anterior
            </button>
            <span>Página {consulta.page + 1}</span>
            <button
              className="button secondary"
              disabled={
                items.length < consulta.size ||
                (consulta.page + 1) * consulta.size >=
                  (query.data.totalItems ?? Infinity)
              }
              onClick={() =>
                setConsulta(
                  new ConsultaLivros(consulta.termo, consulta.page + 1),
                )
              }
            >
              Próxima →
            </button>
          </nav>
        </>
      )}
    </>
  );
}
