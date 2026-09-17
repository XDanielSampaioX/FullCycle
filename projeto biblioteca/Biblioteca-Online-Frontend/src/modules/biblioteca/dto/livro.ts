import { obrigatorio } from "@/shared/domain/campos";
export interface DownloadAccess {
  isAvailable: boolean | null;
  downloadLink: string | null;
}
export interface Livro {
  id: string;
  volumeInfo: {
    title: string | null;
    subtitle: string | null;
    authors: string[] | null;
    publisher: string | null;
    publishedDate: string | null;
    description: string | null;
    industryIdentifiers: { type: string; identifier: string }[] | null;
    pageCount: number | null;
    categories: string[] | null;
    language: string | null;
    imageLinks: {
      smallThumbnail: string | null;
      thumbnail: string | null;
    } | null;
    previewLink: string | null;
    infoLink: string | null;
  } | null;
  accessInfo: {
    publicDomain: boolean | null;
    epub: DownloadAccess | null;
    pdf: DownloadAccess | null;
    webReaderLink: string | null;
  } | null;
  saleInfo: {
    country: string | null;
    saleability: string | null;
    isEbook: boolean | null;
  } | null;
}
export interface GoogleBooksResponse {
  totalItems: number | null;
  items: Livro[] | null;
}
export class ConsultaLivros {
  readonly termo: string;
  readonly page: number;
  readonly size = 12;
  constructor(termo = "literatura", page = 0) {
    this.termo = obrigatorio(termo.trim(), "Termo de busca");
    if (!Number.isInteger(page) || page < 0)
      throw new Error("Página inválida.");
    this.page = page;
  }
  toSearchParams() {
    return new URLSearchParams({
      termo: this.termo,
      page: String(this.page),
      size: String(this.size),
    }).toString();
  }
}
export function linkSeguro(value?: string | null): string | undefined {
  if (!value) return undefined;
  try {
    const url = new URL(value);
    if (!["https:", "http:"].includes(url.protocol)) return undefined;
    url.protocol = "https:";
    return url.href;
  } catch {
    return undefined;
  }
}
