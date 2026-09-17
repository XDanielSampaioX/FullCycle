import { AcessoLayout } from "@/shared/components/ui";
import { LoginForm } from "@/modules/autenticacao/components/login-form";
export default function Page() {
  return (
    <AcessoLayout>
      <LoginForm />
    </AcessoLayout>
  );
}
