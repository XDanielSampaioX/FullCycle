export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
  }
}

export async function api<T>(
  path: string,
  options: RequestInit = {},
  token?: string,
): Promise<T> {

  const headers = new Headers(options.headers);

  if (options.body) headers.set("Content-Type", "application/json");

  if (token) headers.set("Authorization", `Bearer ${token}`);
  
  let response: Response;

  try {
    response = await fetch(`/api${path}`, {
      ...options,
      headers,
      cache: "no-store",
    });
  } catch (error) {
    if (error instanceof Error && error.name === "AbortError") throw error;
    throw new Error("Não foi possível conectar ao servidor. Tente novamente.");
  }

  if (!response.ok) {
    const body = await response.json().catch(() => null);
    const fallback =
      response.status === 401
        ? "CPF ou senha inválidos, ou sessão expirada. Entre novamente."
        : response.status === 403
          ? "Você não tem permissão para esta operação."
          : response.status >= 500
            ? "Não foi possível concluir a operação. Verifique os dados ou tente novamente mais tarde."
            : "Confira os dados informados e tente novamente.";
    throw new ApiError(
      response.status,
      typeof body?.detail === "string" ? body.detail : fallback,
    );
  }
  return response.json() as Promise<T>;
}
