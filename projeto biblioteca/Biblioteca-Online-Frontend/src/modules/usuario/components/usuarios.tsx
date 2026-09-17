"use client";
import Link from "next/link";
import { useConsulta } from "@/modules/autenticacao/hooks/use-consulta";
import { ConsultaStatus } from "@/shared/components/consulta-status";
import { Aviso } from "@/shared/components/ui";
import { listarUsuarios } from "../services/usuario-api";
export function Usuarios() {
  const query = useConsulta(listarUsuarios);
  return (
    <>
      <div className="section-heading">
        <div>
          <p className="eyebrow">NOSSA COMUNIDADE</p>
          <h1>Usuários</h1>
          <p className="muted">Pessoas que fazem parte da biblioteca.</p>
        </div>
        <Link className="button" href="/usuarios/novo">
          + Cadastrar usuário
        </Link>
      </div>
      <ConsultaStatus {...query} />
      {query.data?.length === 0 && <Aviso>Nenhum usuário cadastrado.</Aviso>}
      {!!query.data?.length && (
        <div className="table-wrap panel">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>CPF</th>
                <th>Cidade / UF</th>
              </tr>
            </thead>
            <tbody>
              {query.data.map((usuario) => (
                <tr key={usuario.id}>
                  <td>{usuario.nome}</td>
                  <td>
                    {usuario.cpf.replace(
                      /^(\d{3})(\d{3})(\d{3})(\d{2})$/,
                      "$1.$2.$3-$4",
                    )}
                  </td>
                  <td>
                    {[usuario.endereco?.localidade, usuario.endereco?.uf]
                      .filter(Boolean)
                      .join(" / ") || "Não informado"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
