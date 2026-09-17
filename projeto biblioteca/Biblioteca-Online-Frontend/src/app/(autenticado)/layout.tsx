import { AreaAutenticada } from "@/modules/autenticacao/components/area-autenticada";
export default function Layout({ children }: { children: React.ReactNode }) {
  return <AreaAutenticada>{children}</AreaAutenticada>;
}
