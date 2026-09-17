"use client";

import { useCallback } from "react";
import Link from "next/link";
import { useConsulta } from "@/modules/autenticacao/hooks/use-consulta";
import { ConsultaStatus } from "@/shared/components/consulta-status";
import { buscarLivro } from "../services/biblioteca-api";
import { linkSeguro } from "../dto/livro";
import { Capa } from "./capa";

export function LivroDetalhe({ id }: { id: string }) {
  const load = useCallback(
    (token: string, signal: AbortSignal) => buscarLivro(id, token, signal),
    [id],
  );
  const query = useConsulta(load);
  const livro = query.data;
  const info = livro?.volumeInfo;
  const reader = linkSeguro(
    livro?.accessInfo?.webReaderLink || info?.previewLink,
  );
  const downloads = [
    ["PDF", livro?.accessInfo?.pdf],
    ["EPUB", livro?.accessInfo?.epub],
  ] as const;
  return (
    <>
      <Link className="back-link" href="/catalogo">
        ← Voltar ao catálogo
      </Link>
      <ConsultaStatus {...query} />
      {livro && (
        <article className="detail panel">
          <Capa livro={livro} />
          <div>
            <p className="eyebrow">
              {info?.categories?.join(" · ") || "LIVRO DIGITAL"}
            </p>
            <h1>{info?.title || "Sem título"}</h1>
            {info?.subtitle && <h2>{info.subtitle}</h2>}
            <p className="muted">
              {info?.authors?.join(", ") || "Autoria não informada"}
            </p>
            <dl>
              <div>
                <dt>Editora</dt>
                <dd>{info?.publisher || "Não informada"}</dd>
              </div>
              <div>
                <dt>Publicação</dt>
                <dd>{info?.publishedDate || "Não informada"}</dd>
              </div>
              <div>
                <dt>Páginas</dt>
                <dd>{info?.pageCount ?? "Não informado"}</dd>
              </div>
              <div>
                <dt>Idioma</dt>
                <dd>{info?.language || "Não informado"}</dd>
              </div>
            </dl>
            <h3>Sobre o livro</h3>
            <p className="description">
              {info?.description?.replace(/<[^>]*>/g, "") ||
                "Este livro ainda não possui descrição."}
            </p>
            <div className="actions">
              {reader && (
                <a
                  className="button"
                  href={reader}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Abrir leitura ↗
                </a>
              )}
              {downloads.map(([label, access]) => {
                const href = access?.isAvailable
                  ? linkSeguro(access.downloadLink)
                  : undefined;
                return href ? (
                  <a
                    key={label}
                    className="button secondary"
                    href={href}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    Baixar {label} ↗
                  </a>
                ) : null;
              })}
            </div>
            {!reader && (
              <p className="muted">
                Leitura online não disponível para este título.
              </p>
            )}
          </div>
        </article>
      )}
    </>
  );
}
