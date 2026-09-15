package com.biblioteca.Biblioteca.Online.acervo.service;

import com.biblioteca.Biblioteca.Online.acervo.dto.CatalogoImportacaoResponse;
import com.biblioteca.Biblioteca.Online.acervo.dto.ArquivosImportacaoResponse;
import com.biblioteca.Biblioteca.Online.acervo.dto.LivroCadastroRequest;
import com.biblioteca.Biblioteca.Online.biblioteca.dto.LivroResponse;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class CatalogoCcBySaService {

    private static final String LICENCA = "CC BY-SA 4.0";
    private static final String URL_LICENCA = "https://creativecommons.org/licenses/by-sa/4.0/";
    private static final long TAMANHO_MAXIMO_ZIP = 500L * 1024 * 1024;
    private static final int TAMANHO_MAXIMO_ARQUIVO_FONTE = 500_000;
    private static final int TAMANHO_MAXIMO_TEXTO = 8_000_000;
    private static final int MAXIMO_ARQUIVOS_FONTE = 500;
    private static final float MARGEM_PAGINA = 48;
    private static final float TAMANHO_FONTE = 9;
    private static final float ALTURA_LINHA = 12;

    private final LivroService livroService;

    @Transactional
    public CatalogoImportacaoResponse importar() {
        int inseridos = 0;
        int existentes = 0;
        List<LivroResponse> livros = new ArrayList<>();

        for (LivroCadastroRequest livro : livrosDoCatalogo()) {
            LivroService.RegistroLivro registro = livroService.cadastrarReferenciaSeAusente(livro);
            livros.add(registro.livro());

            if (registro.criado()) {
                inseridos++;
            } else {
                existentes++;
            }
        }

        return new CatalogoImportacaoResponse(inseridos, existentes, livros);
    }

    public ArquivosImportacaoResponse importarArquivosDiretos() {
        CatalogoImportacaoResponse catalogo = importar();
        int importados = 0;
        int existentes = 0;
        List<LivroResponse> livros = new ArrayList<>();
        List<ArquivosImportacaoResponse.FalhaImportacao> falhas = new ArrayList<>();

        for (LivroResponse livro : catalogo.livros()) {
            if (livro.urlDownload() == null) {
                continue;
            }

            if (livro.possuiArquivo()) {
                existentes++;
                livros.add(livro);
                continue;
            }

            try {
                byte[] conteudo = baixarPdf(livro);
                livros.add(livroService.anexarArquivoBaixado(livro.id(), nomeArquivo(livro), conteudo));
                importados++;
            } catch (IllegalStateException | IllegalArgumentException exception) {
                falhas.add(new ArquivosImportacaoResponse.FalhaImportacao(
                        livro.id(),
                        livro.titulo(),
                        livro.urlDownload(),
                        exception.getMessage()
                ));
            }
        }

        return new ArquivosImportacaoResponse(importados, existentes, livros, falhas);
    }

    /**
     * Gera PDFs estaticos para as referencias que nao possuem um PDF direto.
     * O conteudo e lido do ZIP do repositorio e somente arquivos de texto sao
     * convertidos; nenhum script do repositorio e executado.
     */
    public ArquivosImportacaoResponse gerarPdfsDasReferencias() {
        CatalogoImportacaoResponse catalogo = importar();
        int importados = 0;
        int existentes = 0;
        List<LivroResponse> livros = new ArrayList<>();
        List<ArquivosImportacaoResponse.FalhaImportacao> falhas = new ArrayList<>();

        for (LivroResponse livro : catalogo.livros()) {
            if (livro.possuiArquivo()) {
                existentes++;
                livros.add(livro);
                continue;
            }

            try {
                byte[] pdf = gerarPdfDoRepositorio(livro);
                livros.add(livroService.anexarArquivoBaixado(livro.id(), nomeArquivo(livro), pdf));
                importados++;
            } catch (IllegalStateException | IllegalArgumentException exception) {
                falhas.add(new ArquivosImportacaoResponse.FalhaImportacao(
                        livro.id(),
                        livro.titulo(),
                        livro.urlOrigem(),
                        exception.getMessage()
                ));
            }
        }

        return new ArquivosImportacaoResponse(importados, existentes, livros, falhas);
    }

    private byte[] baixarPdf(LivroResponse livro) {
        URI uri = URI.create(livro.urlDownload());

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            ResponseEntity<byte[]> resposta;
            try {
                resposta = RestClient.create()
                        .get()
                        .uri(uri)
                        .retrieve()
                        .toEntity(byte[].class);
            } catch (RestClientException exception) {
                throw new IllegalStateException("Nao foi possivel baixar o PDF de '" + livro.titulo() + "'.", exception);
            }

            if (resposta.getStatusCode().is3xxRedirection()) {
                URI redirecionamento = resposta.getHeaders().getLocation();
                if (redirecionamento == null) {
                    throw new IllegalStateException("A origem redirecionou sem informar o destino para '" + livro.titulo() + "'.");
                }
                uri = redirecionamento;
                continue;
            }

            byte[] conteudo = resposta.getBody();
            if (conteudo == null || conteudo.length == 0) {
                throw new IllegalStateException("A origem nao retornou um PDF para '" + livro.titulo() + "'.");
            }

            return conteudo;
        }

        throw new IllegalStateException("A origem excedeu o limite de redirecionamentos para '" + livro.titulo() + "'.");
    }

    private byte[] gerarPdfDoRepositorio(LivroResponse livro) {
        byte[] zip = baixarRepositorio(livro);
        List<ArquivoFonte> fontes = extrairArquivosDeTexto(zip, livro);

        if (fontes.isEmpty()) {
            throw new IllegalStateException("O repositorio de '" + livro.titulo()
                    + "' nao possui arquivos de texto que possam ser convertidos.");
        }

        return criarPdf(livro, fontes);
    }

    private byte[] baixarRepositorio(LivroResponse livro) {
        URI origem;
        try {
            origem = URI.create(livro.urlOrigem());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("URL de origem invalida para '" + livro.titulo() + "'.", exception);
        }

        if (!"github.com".equalsIgnoreCase(origem.getHost())) {
            throw new IllegalStateException("A origem de '" + livro.titulo()
                    + "' nao e um repositorio GitHub com ZIP automatico.");
        }

        String[] partes = origem.getPath().split("/");
        if (partes.length < 3 || partes[1].isBlank() || partes[2].isBlank()) {
            throw new IllegalStateException("Nao foi possivel identificar o repositorio GitHub de '"
                    + livro.titulo() + "'.");
        }

        String proprietario = partes[1];
        String repositorio = partes[2].replaceFirst("\\.git$", "");
        List<String> branches = List.of("main", "master");
        IllegalStateException ultimaFalha = null;

        for (String branch : branches) {
            URI zipUri = URI.create("https://codeload.github.com/" + proprietario + "/"
                    + repositorio + "/zip/refs/heads/" + branch);
            try {
                byte[] conteudo = baixarBytes(zipUri, livro.titulo(), "ZIP");
                if (conteudo.length > TAMANHO_MAXIMO_ZIP) {
                    throw new IllegalStateException("O ZIP de '" + livro.titulo() + "' excede o limite de 500 MB.");
                }
                return conteudo;
            } catch (IllegalStateException exception) {
                ultimaFalha = exception;
            }
        }

        throw ultimaFalha == null
                ? new IllegalStateException("Nao foi possivel baixar o repositorio de '" + livro.titulo() + "'.")
                : ultimaFalha;
    }

    private byte[] baixarBytes(URI uri, String titulo, String tipo) {
        URI atual = uri;

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            ResponseEntity<byte[]> resposta;
            try {
                resposta = RestClient.create()
                        .get()
                        .uri(atual)
                        .header(HttpHeaders.USER_AGENT, "Biblioteca-Online-educational-importer")
                        .header(HttpHeaders.ACCEPT, "application/octet-stream, */*")
                        .retrieve()
                        .toEntity(byte[].class);
            } catch (RestClientException exception) {
                throw new IllegalStateException("Nao foi possivel baixar o " + tipo.toLowerCase(Locale.ROOT)
                        + " de '" + titulo + "'.", exception);
            }

            if (resposta.getStatusCode().is3xxRedirection()) {
                URI redirecionamento = resposta.getHeaders().getLocation();
                if (redirecionamento == null) {
                    throw new IllegalStateException("A origem redirecionou sem informar o destino para '"
                            + titulo + "'.");
                }
                atual = redirecionamento;
                continue;
            }

            byte[] conteudo = resposta.getBody();
            if (conteudo == null || conteudo.length == 0) {
                throw new IllegalStateException("A origem nao retornou conteudo para '" + titulo + "'.");
            }

            return conteudo;
        }

        throw new IllegalStateException("A origem excedeu o limite de redirecionamentos para '" + titulo + "'.");
    }

    private List<ArquivoFonte> extrairArquivosDeTexto(byte[] zip, LivroResponse livro) {
        List<ArquivoFonte> fontes = new ArrayList<>();
        int totalCaracteres = 0;

        try (ZipInputStream entrada = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry arquivo;
            while ((arquivo = entrada.getNextEntry()) != null && fontes.size() < MAXIMO_ARQUIVOS_FONTE) {
                if (arquivo.isDirectory() || !deveIncluirArquivo(arquivo.getName(), livro)) {
                    continue;
                }

                byte[] conteudo = lerEntradaLimitada(entrada, TAMANHO_MAXIMO_ARQUIVO_FONTE);
                if (conteudo.length == 0) {
                    continue;
                }

                String texto = new String(conteudo, java.nio.charset.StandardCharsets.UTF_8)
                        .replace("\u0000", "");
                if (texto.isBlank()) {
                    continue;
                }

                int restante = TAMANHO_MAXIMO_TEXTO - totalCaracteres;
                if (restante <= 0) {
                    break;
                }
                if (texto.length() > restante) {
                    texto = texto.substring(0, restante) + "\n[conteudo truncado]";
                }

                fontes.add(new ArquivoFonte(removerPrefixoRepositorio(arquivo.getName()), texto));
                totalCaracteres += texto.length();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel ler o ZIP do repositorio de '"
                    + livro.titulo() + "'.", exception);
        }

        fontes.sort(Comparator.comparing(ArquivoFonte::caminho));
        return fontes;
    }

    private byte[] lerEntradaLimitada(ZipInputStream entrada, int limite) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] bloco = new byte[8192];
        int total = 0;
        int lidos;

        while ((lidos = entrada.read(bloco)) != -1) {
            total += lidos;
            if (total > limite) {
                return buffer.toByteArray();
            }
            buffer.write(bloco, 0, lidos);
        }

        return buffer.toByteArray();
    }

    private boolean deveIncluirArquivo(String nome, LivroResponse livro) {
        String normalizado = nome.replace('\\', '/').toLowerCase(Locale.ROOT);
        if (normalizado.contains("/node_modules/")
                || normalizado.contains("/.git/")
                || normalizado.contains("/target/")
                || normalizado.contains("/_build/")) {
            return false;
        }

        if (livro.titulo().startsWith("High-Level System Design")) {
            return normalizado.contains("/content/hld/") && ehArquivoDeTexto(normalizado);
        }
        if (livro.titulo().startsWith("Data Structures & Algorithms")) {
            return normalizado.contains("/content/dsa/") && ehArquivoDeTexto(normalizado);
        }

        return ehArquivoDeTexto(normalizado);
    }

    private boolean ehArquivoDeTexto(String nome) {
        return nome.endsWith(".md")
                || nome.endsWith(".markdown")
                || nome.endsWith(".rst")
                || nome.endsWith(".txt")
                || nome.endsWith(".qmd")
                || nome.endsWith(".rmd")
                || nome.endsWith(".ipynb")
                || nome.endsWith(".org");
    }

    private String removerPrefixoRepositorio(String caminho) {
        int primeiraBarra = caminho.indexOf('/');
        return primeiraBarra >= 0 && primeiraBarra + 1 < caminho.length()
                ? caminho.substring(primeiraBarra + 1)
                : caminho;
    }

    private byte[] criarPdf(LivroResponse livro, List<ArquivoFonte> fontes) {
        try (PDDocument documento = new PDDocument(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            PdfWriter escritor = new PdfWriter(documento);
            escritor.escrever(livro.titulo(), new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            escritor.escrever("Autores: " + livro.autores(), new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            escritor.escrever("Licenca: " + livro.licenca(), new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            escritor.escrever("Fonte: " + livro.urlOrigem(), new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            escritor.escrever("PDF gerado automaticamente a partir dos arquivos textuais do repositorio.",
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 9);
            escritor.linhaEmBranco();

            for (ArquivoFonte fonte : fontes) {
                escritor.escrever("Arquivo: " + fonte.caminho(),
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                escritor.escrever(fonte.texto(), new PDType1Font(Standard14Fonts.FontName.HELVETICA), TAMANHO_FONTE);
                escritor.linhaEmBranco();
            }

            escritor.close();
            documento.save(saida);
            return saida.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel gerar o PDF de '" + livro.titulo() + "'.", exception);
        }
    }

    private record ArquivoFonte(String caminho, String texto) {
    }

    private static final class PdfWriter implements AutoCloseable {

        private final PDDocument documento;
        private final PDFont fontePadrao = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final float larguraPagina;
        private final float alturaPagina;
        private PDPageContentStream fluxo;
        private float y;

        private PdfWriter(PDDocument documento) throws IOException {
            this.documento = documento;
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            this.larguraPagina = pagina.getMediaBox().getWidth();
            this.alturaPagina = pagina.getMediaBox().getHeight();
            this.fluxo = new PDPageContentStream(documento, pagina);
            this.y = alturaPagina - MARGEM_PAGINA;
        }

        private void escrever(String texto, PDFont fonte, float tamanho) throws IOException {
            String[] linhas = texto.replace("\r", "").split("\n", -1);
            float larguraUtil = larguraPagina - (2 * MARGEM_PAGINA);

            for (String linha : linhas) {
                List<String> linhasQuebradas = quebrarLinha(sanitizarTexto(linha), fonte, tamanho, larguraUtil);
                if (linhasQuebradas.isEmpty()) {
                    linhaEmBranco();
                    continue;
                }

                for (String linhaQuebrada : linhasQuebradas) {
                    garantirEspaco(ALTURA_LINHA + 2);
                    fluxo.beginText();
                    fluxo.setFont(fonte, tamanho);
                    fluxo.newLineAtOffset(MARGEM_PAGINA, y);
                    fluxo.showText(linhaQuebrada);
                    fluxo.endText();
                    y -= ALTURA_LINHA;
                }
            }
        }

        private void linhaEmBranco() throws IOException {
            garantirEspaco(ALTURA_LINHA);
            y -= ALTURA_LINHA;
        }

        private List<String> quebrarLinha(String texto, PDFont fonte, float tamanho, float larguraMaxima)
                throws IOException {
            if (texto.isEmpty()) {
                return List.of();
            }

            List<String> resultado = new ArrayList<>();
            StringBuilder atual = new StringBuilder();
            for (String palavra : texto.split("\\s+")) {
                String candidata = atual.isEmpty() ? palavra : atual + " " + palavra;
                if (fonte.getStringWidth(candidata) / 1000 * tamanho > larguraMaxima && !atual.isEmpty()) {
                    resultado.add(atual.toString());
                    atual.setLength(0);
                    atual.append(palavra);
                } else {
                    atual.setLength(0);
                    atual.append(candidata);
                }
            }
            if (!atual.isEmpty()) {
                resultado.add(atual.toString());
            }
            return resultado;
        }

        private void garantirEspaco(float alturaNecessaria) throws IOException {
            if (y - alturaNecessaria >= MARGEM_PAGINA) {
                return;
            }

            fluxo.close();
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            fluxo = new PDPageContentStream(documento, pagina);
            y = alturaPagina - MARGEM_PAGINA;
        }

        private String sanitizarTexto(String texto) {
            StringBuilder resultado = new StringBuilder(texto.length());
            for (int indice = 0; indice < texto.length(); indice++) {
                char caractere = texto.charAt(indice);
                if ((caractere >= 32 && caractere <= 126) || (caractere >= 160 && caractere <= 255)) {
                    resultado.append(caractere);
                } else if (caractere == '\t') {
                    resultado.append("    ");
                } else {
                    resultado.append('?');
                }
            }
            return resultado.toString();
        }

        @Override
        public void close() throws IOException {
            fluxo.close();
        }
    }

    private String nomeArquivo(LivroResponse livro) {
        String semAcentos = Normalizer.normalize(livro.titulo(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String nome = semAcentos
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        return nome + ".pdf";
    }

    private List<LivroCadastroRequest> livrosDoCatalogo() {
        return List.of(
                livro(
                        "Dive into Deep Learning",
                        "Aston Zhang, Zachary C. Lipton, Mu Li e Alexander J. Smola",
                        "Deep learning com exemplos em Python e notebooks.",
                        "Inteligencia artificial e Python",
                        "en",
                        "https://github.com/d2l-ai/d2l-en",
                        "https://d2l.ai/",
                        "https://github.com/d2l-ai/d2l-en/releases/download/v1.0.3/d2l-en-FULL-1.0.3.pdf"
                ),
                livro(
                        "Estrutura e Interpretacao de Programas de Computador — JavaScript",
                        "Ibrahim Cesar; obra original de Harold Abelson e Gerald Jay Sussman",
                        "Traducao e adaptacao em portugues sobre programacao e interpretadores.",
                        "Fundamentos de programacao",
                        "pt-BR",
                        "https://github.com/ibrahimcesar/estrutura-e-interpretacao-de-programas-de-computador-javascript",
                        "https://sicpjs.com/pt_BR/",
                        "https://github.com/ibrahimcesar/estrutura-e-interpretacao-de-programas-de-computador-javascript/releases/download/ebook-2026-07-21/SICP-JS-PT-BR.pdf"
                ),
                livro(
                        "A Byte of Python",
                        "Swaroop C H",
                        "Introducao a programacao com Python.",
                        "Python",
                        "en",
                        "https://github.com/swaroopch/byte-of-python",
                        "https://python.swaroopch.com/",
                        "https://github.com/swaroopch/byte-of-python/releases/download/vb57ea0a5919424d06fd448e78fdcc603af60f3c8/byte-of-python.pdf"
                ),
                livro(
                        "The Aya Book",
                        "Aya contributors",
                        "Introducao a Rust e eBPF com a biblioteca Aya.",
                        "Rust e sistemas",
                        "en",
                        "https://github.com/aya-rs/book",
                        "https://aya-rs.dev/book/",
                        null
                ),
                livro(
                        "Clojure Development with Spacemacs",
                        "Practicalli community",
                        "Desenvolvimento Clojure com Emacs, Spacemacs e REPL.",
                        "Clojure",
                        "en",
                        "https://github.com/practicalli/spacemacs",
                        "https://practical.li/spacemacs/",
                        null
                ),
                livro(
                        "The Book of Robocode",
                        "Flemming Nornberg Larsen e comunidade Robocode",
                        "Programacao de robos para a plataforma Robocode.",
                        "Java e jogos",
                        "en",
                        "https://github.com/robocode-dev/book-of-robocode",
                        null,
                        null
                ),
                livro(
                        "Software Engineering: Standing on the Shoulders of Giants",
                        "Thomas Hastings",
                        "Livro de engenharia de software, arquitetura, testes, qualidade e CI/CD.",
                        "Engenharia de software",
                        "en",
                        "https://github.com/tghastings/open-swe-book",
                        "https://www.swebook.org/",
                        "https://github.com/tghastings/open-swe-book/releases/download/1.0b16/swebook-generic.pdf"
                ),
                livro(
                        "The Software Engineering: DevOps, CI/CD, Docker, Provisioning and Git Flow",
                        "AstroTech",
                        "DevOps, Docker, Git, Bash, cloud e microsservicos.",
                        "DevOps",
                        "en",
                        "https://github.com/astromatt/book-dev",
                        "https://dev.astrotech.io/",
                        null
                ),
                livro(
                        "A Guide to DevOps Engineering: Bridging the Gap",
                        "Baha Tanvir",
                        "Guia de praticas e fundamentos de DevOps.",
                        "DevOps",
                        "en",
                        "https://github.com/BahaTanvir/devops-guide-book",
                        null,
                        null
                ),
                livro(
                        "High-Level System Design Handbook",
                        "Handbook Academy",
                        "Arquitetura de sistemas, confiabilidade, seguranca, cloud e sistemas distribuidos.",
                        "Arquitetura de software",
                        "en",
                        "https://github.com/handbook-academy/engineering-handbook",
                        "https://handbook.academy/",
                        null
                ),
                livro(
                        "Data Structures & Algorithms Handbook",
                        "Handbook Academy",
                        "Estruturas de dados, algoritmos, grafos e programacao dinamica.",
                        "Algoritmos",
                        "en",
                        "https://github.com/handbook-academy/engineering-handbook",
                        "https://handbook.academy/",
                        null
                ),
                livro(
                        "Computer Science Field Guide",
                        "University of Canterbury Computer Science Education Research Group",
                        "Material interativo de fundamentos de ciencia da computacao.",
                        "Ciencia da computacao",
                        "en",
                        "https://github.com/uccser/cs-field-guide",
                        "https://www.csfieldguide.org.nz/",
                        null
                ),
                livro(
                        "Mathematical Programming and Operations Research",
                        "Robert Hildebrand",
                        "Modelagem, algoritmos e otimizacao com exemplos em Python e Excel.",
                        "Algoritmos e otimizacao",
                        "en",
                        "https://github.com/open-optimization/open-optimization-or-book",
                        "https://open-optimization.github.io/open-optimization-or-book/",
                        "https://open-optimization.github.io/open-optimization-or-book/Intro-Math-Programming/baseText/book/book1-main.pdf"
                ),
                livro(
                        "Stas' Python Cookbook",
                        "Stas Bekman",
                        "Receitas praticas de Python, testes, profiling e empacotamento.",
                        "Python",
                        "en",
                        "https://github.com/stas00/python-cookbook",
                        null,
                        "https://huggingface.co/stas/python-cookbook/resolve/main/Stas%20Bekman%20-%20Stas%27%20Python%20Cookbook.pdf?download=true"
                ),
                livro(
                        "Machine Learning Engineering Open Book",
                        "Stas Bekman",
                        "Infraestrutura, treinamento e inferencia para machine learning.",
                        "Machine learning",
                        "en",
                        "https://github.com/stas00/ml-engineering",
                        null,
                        "https://huggingface.co/stas/ml-engineering-book/resolve/main/Stas%20Bekman%20-%20Machine%20Learning%20Engineering.pdf?download=true"
                ),
                livro(
                        "The Art of Debugging Open Book",
                        "Stas Bekman",
                        "Depuracao em Unix, Python e PyTorch.",
                        "Depuracao",
                        "en",
                        "https://github.com/stas00/the-art-of-debugging",
                        null,
                        "https://huggingface.co/stas/the-art-of-debugging-book/resolve/main/Stas%20Bekman%20-%20The%20Art%20of%20Debugging.pdf?download=true"
                ),
                livro(
                        "Reproducible Machine Learning for Credit Card Fraud Detection",
                        "Fraud Detection Handbook contributors",
                        "Handbook pratico de machine learning reproduzivel com notebooks.",
                        "Machine learning",
                        "en",
                        "https://github.com/Fraud-Detection-Handbook/fraud-detection-handbook",
                        null,
                        null
                ),
                livro(
                        "Answering Questions with Data",
                        "Matthew J. Crump",
                        "Estatistica computacional com R.",
                        "Dados e R",
                        "en",
                        "https://github.com/CrumpLab/statistics",
                        "https://crumplab.github.io/statistics/",
                        null
                ),
                livro(
                        "Data Storytelling",
                        "Eric Culler",
                        "Narrativas e analise de dados em Jupyter Book.",
                        "Dados",
                        "en",
                        "https://github.com/eculler/data_storytelling",
                        null,
                        null
                ),
                livro(
                        "Data Science Live Book",
                        "Pablo Casas",
                        "Ciencia de dados, analise e machine learning.",
                        "Ciencia de dados",
                        "en",
                        "https://github.com/pablo14/data-science-live-book",
                        "https://livebook.datascienceheroes.com/",
                        null
                ),
                livro(
                        "AI Agent Architecture",
                        "Drobiazkin",
                        "Arquitetura de agentes, RAG, MCP e seguranca.",
                        "Inteligencia artificial",
                        "en",
                        "https://github.com/Drobiazkin/ai-agent-architecture",
                        null,
                        "https://raw.githubusercontent.com/Drobiazkin/ai-agent-architecture/e500f805745b08fa1bf496a29dfc2537ce658acd/ai-agent-architecture-book.pdf"
                ),
                livro(
                        "Enterprise AI Agents on LangGraph",
                        "Aaron Roe",
                        "Padroes para agentes corporativos com LangGraph.",
                        "Inteligencia artificial",
                        "en",
                        "https://github.com/AaronRoeF/enterprise-ai-agents-on-langgraph",
                        null,
                        null
                ),
                livro(
                        "Steadfast Self-Hosting: Rapid-Rise Personal Cloud",
                        "meonkeys",
                        "Auto-hospedagem e operacoes de infraestrutura pessoal.",
                        "Infraestrutura",
                        "en",
                        "https://github.com/meonkeys/shb",
                        null,
                        null
                ),
                livro(
                        "Learning Statistics with jamovi",
                        "David Foxcroft",
                        "Estatistica aplicada e uso do software jamovi.",
                        "Dados e estatistica",
                        "en",
                        "https://github.com/davidfoxcroft/lsj-book",
                        null,
                        "https://github.com/user-attachments/files/18124061/learning-statistics-with-jamovi-0.75.pdf"
                ),
                livro(
                        "The Art of Command Line",
                        "Joshua Levy e colaboradores",
                        "Guia de Bash, Linux, Git, SSH e processamento de arquivos.",
                        "Linha de comando",
                        "pt-BR",
                        "https://github.com/jlevy/the-art-of-command-line",
                        "https://github.com/jlevy/the-art-of-command-line/blob/master/README-pt.md",
                        null
                )
        );
    }

    private LivroCadastroRequest livro(
            String titulo,
            String autores,
            String descricao,
            String categoria,
            String idioma,
            String urlOrigem,
            String urlLeitura,
            String urlDownload
    ) {
        String atribuicao = autores + ". " + titulo + ". Disponibilizado sob " + LICENCA
                + ". Fonte: " + urlOrigem;

        return new LivroCadastroRequest(
                titulo,
                autores,
                descricao,
                categoria,
                idioma,
                LICENCA,
                URL_LICENCA,
                atribuicao,
                urlOrigem,
                urlLeitura,
                urlDownload
        );
    }
}
