import { Aviso } from "./ui";

export function ConsultaStatus({
  loading,
  error,
  retry,
}: {
  loading: boolean;
  error?: string;
  retry: () => void;
}) {
  if (loading) return <Aviso>Carregando…</Aviso>;
  if (error)
    return (
      <div>
        <Aviso erro>{error}</Aviso>
        <button className="button secondary" onClick={retry}>
          Tentar novamente
        </button>
      </div>
    );
  return null;
}
