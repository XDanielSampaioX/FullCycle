import Link from "next/link";
import type { InputHTMLAttributes, ReactNode } from "react";
export function Marca() {
  return (
    <Link className="brand" href="/catalogo">
      <span className="brand-icon" aria-hidden="true">
        ▤
      </span>
      <span>
        Biblioteca<span className="brand-light"> Online</span>
      </span>
    </Link>
  );
}
export function Campo({
  label,
  name,
  ...props
}: InputHTMLAttributes<HTMLInputElement> & { label: string; name: string }) {
  return (
    <label className="field" htmlFor={name}>
      <span>{label}</span>
      <input id={name} name={name} {...props} />
    </label>
  );
}
export function Aviso({
  children,
  erro = false,
}: {
  children: ReactNode;
  erro?: boolean;
}) {
  return (
    <p
      className={`notice ${erro ? "error" : ""}`}
      role={erro ? "alert" : "status"}
    >
      {children}
    </p>
  );
}
export function AcessoLayout({ children }: { children: ReactNode }) {
  return (
    <main className="access">
      <section className="access-story">
        <Marca />
        <div>
          <p className="eyebrow">UM MUNDO DE HISTÓRIAS</p>
          <h1>
            Abra um livro.
            <br />
            Descubra um
            <br />
            <em>novo mundo.</em>
          </h1>
          <p>
            Grandes ideias e boas histórias, ao seu alcance.
            <br />
            Seu próximo capítulo começa aqui.
          </p>
          <div className="book-art" aria-hidden="true">
            <span>HISTÓRIAS</span>
            <span>IDEIAS</span>
            <span>DESCOBERTAS</span>
          </div>
        </div>
        <small>Biblioteca Online · Conhecimento para todos</small>
      </section>
      <section className="access-form">{children}</section>
    </main>
  );
}
