"use client";
import { useState, type FormEvent } from "react";
import Link from "next/link";
import { Campo, Aviso } from "@/shared/components/ui";
import { criarUsuario } from "../dto/usuario";
import { cadastrarUsuario } from "../services/usuario-api";
export function CadastroForm({ interno = false }: { interno?: boolean }) {
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [busy, setBusy] = useState(false);
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    setError("");
    setSuccess("");
    setBusy(true);
    try {
      const usuario = await cadastrarUsuario(criarUsuario(new FormData(form)));
      setSuccess(`${usuario.nome}, cadastro realizado com sucesso!`);
      form.reset();
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Não foi possível cadastrar.",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="form-content">
      <p className="eyebrow">{interno ? "USUÁRIOS" : "COMECE SUA JORNADA"}</p>
      <h2>{interno ? "Cadastrar novo usuário" : "Crie sua conta"}</h2>
      <p className="muted">Uma biblioteca de possibilidades espera por você.</p>
      <form onSubmit={submit}>
        <Campo label="Nome completo" name="nome" autoComplete="name" required />
        <div className="form-grid">
          <Campo
            label="CPF"
            name="cpf"
            placeholder="000.000.000-00"
            inputMode="numeric"
            maxLength={14}
            autoComplete="username"
            required
          />
          <Campo
            label="Senha"
            name="senha"
            type="password"
            minLength={8}
            placeholder="Pelo menos 8 caracteres"
            autoComplete="new-password"
            required
          />
        </div>
        <fieldset>
          <legend>Endereço</legend>
          <p className="muted small">
            A cidade e a rua serão consultadas pelo CEP.
          </p>
          <div className="form-grid">
            <Campo
              label="CEP"
              name="cep"
              placeholder="00000-000"
              inputMode="numeric"
              maxLength={9}
              autoComplete="postal-code"
              required
            />
            <Campo
              label="Número"
              name="numero"
              placeholder="Ex.: 123 ou S/N"
              required
            />
          </div>
          <Campo
            label="Complemento (opcional)"
            name="complemento"
            placeholder="Apartamento, bloco…"
            autoComplete="address-line2"
          />
        </fieldset>
        {error && <Aviso erro>{error}</Aviso>}
        {success && (
          <Aviso>
            {success}{" "}
            <Link href={interno ? "/usuarios" : "/login"}>
              {interno ? "Ver usuários" : "Fazer login"} →
            </Link>
          </Aviso>
        )}
        <button className="button wide" disabled={busy}>
          {busy
            ? "Cadastrando…"
            : interno
              ? "Cadastrar usuário →"
              : "Criar minha conta →"}
        </button>
      </form>
      <p className="form-foot">
        <Link href={interno ? "/usuarios" : "/login"}>
          {interno ? "← Voltar para usuários" : "Já tem uma conta? Entre aqui"}
        </Link>
      </p>
    </div>
  );
}
