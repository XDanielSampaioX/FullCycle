import { LivroDetalhe } from "@/modules/biblioteca/components/livro-detalhe";

export default async function Page({ params, }: {
  params: Promise<{ id: string }>;
}) {

  const { id } = await params;
  return <LivroDetalhe id={id} />;
}
