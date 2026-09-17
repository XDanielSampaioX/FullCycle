"use client";

import { useEffect, useState } from "react";
import { ApiError } from "@/shared/http/api";
import { useAuth } from "./use-auth";

export function useConsulta<T>(load: (token: string, signal: AbortSignal) => Promise<T>,) {

  const { session, encerrarSessao } = useAuth();
  const [attempt, setAttempt] = useState(0);
  const [result, setResult] = useState<{
    load: typeof load;
    attempt: number;
    token: string;
    data?: T;
    error?: string;
  }>();

  useEffect(() => {

    if (!session) return;
    const controller = new AbortController();

    load(session.token, controller.signal)
      .then((data) => {
        if (!controller.signal.aborted)
          setResult({ load, attempt, token: session.token, data });
      })
      .catch((error) => {
        if (controller.signal.aborted) return;
        if (error instanceof ApiError && error.status === 401) encerrarSessao();
        else
          setResult({
            load,
            attempt,
            token: session.token,
            error:
              error instanceof Error
                ? error.message
                : "Não foi possível carregar os dados.",
          });
      });
    return () => controller.abort();
  }, [session, load, attempt, encerrarSessao]);
  const current =
    result?.load === load &&
      result.attempt === attempt &&
      result.token === session?.token
      ? result
      : undefined;
  return {
    data: current?.data,
    error: current?.error,
    loading: !current,
    retry: () => setAttempt((value) => value + 1),
  };
}
