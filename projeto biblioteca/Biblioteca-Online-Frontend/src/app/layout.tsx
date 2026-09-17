import type { Metadata } from "next";
import "./globals.css";
export const metadata: Metadata = {
  title: "Biblioteca Online",
  description: "Seu próximo capítulo começa aqui. Explore livros gratuitos.",
};
export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
