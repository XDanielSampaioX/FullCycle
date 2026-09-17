"use client";

import { useEffect, type ReactNode } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { encerrarSessao } from "../session";
import { useSessao } from "../hooks/use-sessao";
import { Marca, Aviso } from "@/shared/components/ui";

export function AreaAutenticada({ children }: { children: ReactNode }) {
  const session = useSessao();
  const router = useRouter();
  const pathname = usePathname();
  useEffect(() => {
    if (session === null) router.replace("/login");
    if (!session) return;
    const timer = window.setTimeout(
      encerrarSessao,
      Math.max(0, session.expiresAt - Date.now()),
    );
    return () => window.clearTimeout(timer);
  }, [session, router]);
  if (!session)
    return (
      <main className="container">
        <Aviso>Verificando sua sessão…</Aviso>
      </main>
    );
  return (
    <>
      <header className="header">
        <Marca />
        <nav aria-label="Navegação principal">
          <Link
            aria-current={pathname.startsWith("/catalogo") ? "page" : undefined}
            href="/catalogo"
          >
            Catálogo
          </Link>
          <Link
            aria-current={pathname.startsWith("/usuarios") ? "page" : undefined}
            href="/usuarios"
          >
            Usuários
          </Link>
          <button className="text-button" onClick={encerrarSessao}>
            Sair ↗
          </button>
        </nav>
      </header>
      <main className="container">{children}</main>
      <footer>
        Biblioteca Online <span>Uma nova descoberta a cada página.</span>
      </footer>
    </>
  );
}
