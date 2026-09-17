"use client";
import { useState } from "react";
import { linkSeguro, type Livro } from "../dto/livro";
export function Capa({ livro }: { livro: Livro }) {
  const [failed, setFailed] = useState(false);
  const src = linkSeguro(
    livro.volumeInfo?.imageLinks?.thumbnail ||
      livro.volumeInfo?.imageLinks?.smallThumbnail,
  );
  return (
    <div className="cover">
      {src && !failed ? (
        // Imagens externas variáveis fornecidas pelo catálogo, sem proxy de otimização.
        // eslint-disable-next-line @next/next/no-img-element
        <img
          src={src}
          alt={`Capa de ${livro.volumeInfo?.title || "livro"}`}
          loading="lazy"
          onError={() => setFailed(true)}
        />
      ) : (
        <div className="cover-fallback">
          <span aria-hidden="true">▤</span>
          <strong>{livro.volumeInfo?.title || "Sem título"}</strong>
          <small>Biblioteca Online</small>
        </div>
      )}
    </div>
  );
}
