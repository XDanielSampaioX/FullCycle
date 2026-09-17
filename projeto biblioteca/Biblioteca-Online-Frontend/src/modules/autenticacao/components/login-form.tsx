"use client";
import { useState, type FormEvent } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Campo, Aviso } from "@/shared/components/ui";
import { criarLogin } from "../dto/login";
import { autenticar } from "../services/autenticacao-api";
import { iniciarSessao } from "../session";
export function LoginForm() {
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const router = useRouter();
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setBusy(true);
    try {
      iniciarSessao(
        await autenticar(criarLogin(new FormData(event.currentTarget))),
      );
      router.replace("/catalogo");
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Não foi possível entrar.",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="form-content">
      <p className="eyebrow">BEM-VINDO DE VOLTA</p>
      <h2>Entre na sua biblioteca</h2>
      <p className="muted">Suas próximas descobertas estão esperando.</p>
      <form onSubmit={submit}>
        <Campo
          label="CPF"
          name="cpf"
          placeholder="000.000.000-00"
          autoComplete="username"
          inputMode="numeric"
          maxLength={14}
          required
        />
        <Campo
          label="Senha"
          name="senha"
          type="password"
          placeholder="Digite sua senha"
          autoComplete="current-password"
          required
        />
        {error && <Aviso erro>{error}</Aviso>}
        <button disabled={busy} className="button wide">
          {busy ? "Entrando…" : "Entrar na biblioteca →"}
        </button>
      </form>
      <p className="form-foot">
        Ainda não tem uma conta? <Link href="/cadastro">Cadastre-se</Link>
      </p>
    </div>
  );
}
