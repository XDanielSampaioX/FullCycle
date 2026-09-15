package com.biblioteca.Biblioteca.Online.acervo.service;

import com.biblioteca.Biblioteca.Online.acervo.domain.LivroArquivoEntity;
import com.biblioteca.Biblioteca.Online.acervo.domain.LivroCapaEntity;
import com.biblioteca.Biblioteca.Online.acervo.dto.LivroCadastroRequest;
import com.biblioteca.Biblioteca.Online.acervo.repository.LivroArquivoRepository;
import com.biblioteca.Biblioteca.Online.acervo.repository.LivroCapaRepository;
import com.biblioteca.Biblioteca.Online.biblioteca.domain.LivroEntity;
import com.biblioteca.Biblioteca.Online.biblioteca.dto.LivroResponse;
import com.biblioteca.Biblioteca.Online.biblioteca.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LivroService {

    private static final long TAMANHO_MAXIMO_PDF = 500L * 1024 * 1024;
    private static final long TAMANHO_MAXIMO_CAPA = 20L * 1024 * 1024;
    private static final byte[] ASSINATURA_PDF = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final LivroRepository livroRepository;
    private final LivroArquivoRepository livroArquivoRepository;
    private final LivroCapaRepository livroCapaRepository;

    public LivroResponse cadastrar(LivroCadastroRequest request, MultipartFile arquivo) {
        DadosArquivo dadosArquivo = lerPdf(arquivo);
        validarHashDisponivel(dadosArquivo.hashArquivo(), null);

        LivroEntity livro = livroRepository.save(criarEntidade(request));
        LivroArquivoEntity arquivoSalvo = salvarArquivo(livro, dadosArquivo);

        return paraResponse(livro, arquivoSalvo, null);
    }

    public RegistroLivro cadastrarReferenciaSeAusente(LivroCadastroRequest request) {
        return livroRepository.findByTituloAndUrlOrigem(request.titulo(), request.urlOrigem())
                .map(livro -> {
                    atualizarMetadados(livro, request);
                    livroRepository.save(livro);
                    return new RegistroLivro(paraResponse(livro), false);
                })
                .orElseGet(() -> {
                    LivroEntity livro = livroRepository.save(criarEntidade(request));
                    return new RegistroLivro(paraResponse(livro), true);
                });
    }

    public LivroResponse anexarArquivo(UUID id, MultipartFile arquivo) {
        LivroEntity livro = buscarEntidade(id);
        DadosArquivo dadosArquivo = lerPdf(arquivo);
        validarHashDisponivel(dadosArquivo.hashArquivo(), id);

        LivroArquivoEntity arquivoSalvo = salvarArquivo(livro, dadosArquivo);
        LivroCapaEntity capa = livroCapaRepository.findById(id).orElse(null);
        return paraResponse(livro, arquivoSalvo, capa);
    }

    public LivroResponse anexarArquivoBaixado(UUID id, String nomeArquivo, byte[] conteudo) {
        LivroEntity livro = buscarEntidade(id);
        DadosArquivo dadosArquivo = validarPdf(nomeArquivo, conteudo);
        validarHashDisponivel(dadosArquivo.hashArquivo(), id);

        LivroArquivoEntity arquivoSalvo = salvarArquivo(livro, dadosArquivo);
        LivroCapaEntity capa = livroCapaRepository.findById(id).orElse(null);
        return paraResponse(livro, arquivoSalvo, capa);
    }

    public void anexarCapa(UUID id, MultipartFile arquivo) {
        LivroEntity livro = buscarEntidade(id);
        DadosCapa dadosCapa = lerCapa(arquivo);
        validarHashCapaDisponivel(dadosCapa.hashCapa(), id);

        LivroCapaEntity capa = livroCapaRepository.findById(id).orElseGet(LivroCapaEntity::new);
        capa.setLivro(livro);
        capa.setLivroId(livro.getId());
        capa.setNomeCapa(dadosCapa.nomeCapa());
        capa.setTipoConteudo(dadosCapa.tipoConteudo());
        capa.setTamanhoBytes((long) dadosCapa.conteudo().length);
        capa.setHashCapa(dadosCapa.hashCapa());
        capa.setArquivoCapa(dadosCapa.conteudo());

        livroCapaRepository.save(capa);
    }

    private LivroEntity buscarEntidade(UUID id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Livro nao encontrado."));
    }

    private LivroEntity criarEntidade(LivroCadastroRequest request) {
        LivroEntity livro = new LivroEntity();
        atualizarMetadados(livro, request);
        return livro;
    }

    private void atualizarMetadados(LivroEntity livro, LivroCadastroRequest request) {
        livro.setTitulo(request.titulo().trim());
        livro.setAutores(request.autores().trim());
        livro.setDescricao(normalizarOpcional(request.descricao()));
        livro.setCategoria(request.categoria().trim());
        livro.setIdioma(request.idioma().trim());
        livro.setLicenca(request.licenca().trim());
        livro.setUrlLicenca(request.urlLicenca().trim());
        livro.setAtribuicao(request.atribuicao().trim());
        livro.setUrlOrigem(request.urlOrigem().trim());
        livro.setUrlLeitura(normalizarOpcional(request.urlLeitura()));
        livro.setUrlDownload(normalizarOpcional(request.urlDownload()));
    }

    private String normalizarOpcional(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : null;
    }

    private DadosArquivo lerPdf(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Envie um arquivo PDF nao vazio.");
        }

        try {
            return validarPdf(extrairNomeArquivo(arquivo.getOriginalFilename()), arquivo.getBytes());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Nao foi possivel ler o arquivo enviado.", exception);
        }
    }

    private DadosCapa lerCapa(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Envie uma imagem de capa nao vazia.");
        }

        String tipoConteudo = arquivo.getContentType();
        if (!StringUtils.hasText(tipoConteudo) || !tipoConteudo.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("A capa deve ser uma imagem.");
        }

        byte[] conteudo;
        try {
            conteudo = arquivo.getBytes();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Nao foi possivel ler a capa enviada.", exception);
        }

        if (conteudo.length > TAMANHO_MAXIMO_CAPA) {
            throw new IllegalArgumentException("A capa excede o limite de 20 MB.");
        }

        return new DadosCapa(
                extrairNomeArquivo(arquivo.getOriginalFilename()),
                tipoConteudo,
                conteudo,
                calcularSha256(conteudo)
        );
    }

    private DadosArquivo validarPdf(String nomeArquivo, byte[] conteudo) {
        if (conteudo == null || conteudo.length == 0) {
            throw new IllegalArgumentException("Envie um arquivo PDF nao vazio.");
        }

        if (conteudo.length > TAMANHO_MAXIMO_PDF) {
            throw new IllegalArgumentException("O PDF excede o limite de 500 MB.");
        }

        String nomeLimpo = extrairNomeArquivo(nomeArquivo);
        if (!nomeLimpo.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Somente arquivos com extensao .pdf sao aceitos.");
        }

        if (!temAssinaturaPdf(conteudo)) {
            throw new IllegalArgumentException("O arquivo enviado nao possui uma assinatura PDF valida.");
        }

        return new DadosArquivo(
                nomeLimpo,
                MediaType.APPLICATION_PDF_VALUE,
                conteudo,
                calcularSha256(conteudo)
        );
    }

    private String extrairNomeArquivo(String nomeOriginal) {
        String nomeLimpo = StringUtils.cleanPath(nomeOriginal == null ? "livro.pdf" : nomeOriginal)
                .replace('\\', '/');
        int ultimaBarra = nomeLimpo.lastIndexOf('/');
        String nomeArquivo = ultimaBarra >= 0 ? nomeLimpo.substring(ultimaBarra + 1) : nomeLimpo;

        if (!StringUtils.hasText(nomeArquivo) || ".".equals(nomeArquivo)) {
            return "livro.pdf";
        }

        return nomeArquivo;
    }

    private boolean temAssinaturaPdf(byte[] conteudo) {
        if (conteudo.length < ASSINATURA_PDF.length) {
            return false;
        }

        for (int indice = 0; indice < ASSINATURA_PDF.length; indice++) {
            if (conteudo[indice] != ASSINATURA_PDF[indice]) {
                return false;
            }
        }

        return true;
    }

    private String calcularSha256(byte[] conteudo) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(conteudo);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 nao esta disponivel na JVM.", exception);
        }
    }

    private void validarHashDisponivel(String hashArquivo, UUID idDoLivroAtual) {
        livroArquivoRepository.findByHashArquivo(hashArquivo)
                .filter(arquivo -> !Objects.equals(arquivo.getLivroId(), idDoLivroAtual))
                .ifPresent(arquivo -> {
                    throw new IllegalArgumentException(
                            "Este PDF ja esta associado ao livro '" + arquivo.getLivro().getTitulo() + "'."
                    );
                });
    }

    private void validarHashCapaDisponivel(String hashCapa, UUID idDoLivroAtual) {
        livroCapaRepository.findByHashCapa(hashCapa)
                .filter(capa -> !Objects.equals(capa.getLivroId(), idDoLivroAtual))
                .ifPresent(capa -> {
                    throw new IllegalArgumentException(
                            "Esta capa ja esta associada ao livro '" + capa.getLivro().getTitulo() + "'."
                    );
                });
    }

    private LivroArquivoEntity salvarArquivo(LivroEntity livro, DadosArquivo dadosArquivo) {
        LivroArquivoEntity arquivo = livroArquivoRepository.findById(livro.getId())
                .orElseGet(LivroArquivoEntity::new);
        arquivo.setLivro(livro);
        arquivo.setLivroId(livro.getId());
        arquivo.setNomeArquivo(dadosArquivo.nomeArquivo());
        arquivo.setTipoConteudo(dadosArquivo.tipoConteudo());
        arquivo.setTamanhoBytes((long) dadosArquivo.conteudo().length);
        arquivo.setHashArquivo(dadosArquivo.hashArquivo());
        arquivo.setArquivoPdf(dadosArquivo.conteudo());
        return livroArquivoRepository.save(arquivo);
    }

    private LivroResponse paraResponse(LivroEntity livro) {
        LivroArquivoEntity arquivo = livro.getId() == null
                ? null
                : livroArquivoRepository.findById(livro.getId()).orElse(null);
        LivroCapaEntity capa = livro.getId() == null
                ? null
                : livroCapaRepository.findById(livro.getId()).orElse(null);
        return paraResponse(livro, arquivo, capa);
    }

    private LivroResponse paraResponse(
            LivroEntity livro,
            LivroArquivoEntity arquivo,
            LivroCapaEntity capa
    ) {
        return new LivroResponse(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutores(),
                livro.getDescricao(),
                livro.getCategoria(),
                livro.getIdioma(),
                livro.getLicenca(),
                livro.getUrlLicenca(),
                livro.getAtribuicao(),
                livro.getUrlOrigem(),
                livro.getUrlLeitura(),
                livro.getUrlDownload(),
                arquivo != null,
                arquivo == null ? null : arquivo.getNomeArquivo(),
                arquivo == null ? null : arquivo.getTipoConteudo(),
                arquivo == null ? null : arquivo.getTamanhoBytes(),
                capa != null,
                capa == null ? null : capa.getNomeCapa(),
                capa == null ? null : capa.getTipoConteudo(),
                capa == null ? null : capa.getTamanhoBytes(),
                livro.getCriadoEm(),
                livro.getAtualizadoEm()
        );
    }

    public record RegistroLivro(LivroResponse livro, boolean criado) {
    }

    private record DadosArquivo(String nomeArquivo, String tipoConteudo, byte[] conteudo, String hashArquivo) {
    }

    private record DadosCapa(String nomeCapa, String tipoConteudo, byte[] conteudo, String hashCapa) {
    }
}
