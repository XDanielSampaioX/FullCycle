"use client";

import { createContext, useMemo, useSyncExternalStore, type ReactNode } from "react";
import { read, subscribe, iniciarSessao, encerrarSessao } from "../session";

type AuthContextValue = {
  session: ReturnType<typeof read> | undefined;
  iniciarSessao: typeof iniciarSessao;
  encerrarSessao: typeof encerrarSessao;
};

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const serverSnapshot = () => undefined;

export function AuthProvider({ children }: { children: ReactNode }) {
  const session = useSyncExternalStore(subscribe, read, serverSnapshot);
  const value = useMemo(
    () => ({ session, iniciarSessao, encerrarSessao }),
    [session],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
