export function texto(form: FormData, name: string): string {
  return String(form.get(name) ?? "").trim();
}
export function digitos(value: string, length: number, label: string): string {
  const result = value.replace(/[.\-\s]/g, "");
  if (!new RegExp(`^\\d{${length}}$`).test(result))
    throw new Error(`${label} deve conter ${length} dígitos.`);
  return result;
}
export function obrigatorio(value: string, label: string): string {
  if (!value.trim()) throw new Error(`${label} é obrigatório.`);
  return value;
}
