package com.biblioteca.Biblioteca.Online.biblioteca.service;

import com.biblioteca.Biblioteca.Online.acervo.domain.LivroArquivoEntity;
import com.biblioteca.Biblioteca.Online.acervo.domain.LivroCapaEntity;
import com.biblioteca.Biblioteca.Online.acervo.repository.LivroArquivoRepository;
import com.biblioteca.Biblioteca.Online.acervo.repository.LivroCapaRepository;
import com.biblioteca.Biblioteca.Online.biblioteca.dto.LivroResponse;
import com.biblioteca.Biblioteca.Online.biblioteca.repository.LivroRepository;
import com.biblioteca.Biblioteca.Online.biblioteca.repository.LivroConsultaProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

/** Consultas de leitura e download dos livros persistidos no banco. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LivroConsultaService {

    private final LivroRepository livroRepository;
    private final LivroArquivoRepository livroArquivoRepository;
    private final LivroCapaRepository livroCapaRepository;

    public Page<LivroResponse> buscar(String termo, Pageable pageable) {
        Page<LivroConsultaProjection> livros = termo == null || termo.isEmpty()
                ? livroRepository.findAllBy(pageable)
                : livroRepository.buscarPorTermo(termo, pageable);

        return livros.map(this::paraResponse);
    }

    public LivroResponse buscarPorId(UUID id) {
        return livroRepository.buscarProjecaoPorId(id)
                .map(this::paraResponse)
                .orElseThrow(() -> new NoSuchElementException("Livro nao encontrado."));
    }

    public ArquivoLivro baixarArquivo(UUID id) {
        LivroArquivoEntity arquivo = livroArquivoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Este livro ainda nao possui um PDF armazenado."));
        byte[] conteudo = arquivo.getArquivoPdf();

        if (conteudo == null || conteudo.length == 0) {
            throw new NoSuchElementException("Este livro ainda nao possui um PDF armazenado.");
        }

        String tipoConteudo = arquivo.getTipoConteudo() != null && !arquivo.getTipoConteudo().isEmpty()
                ? arquivo.getTipoConteudo()
                : "application/pdf";
        String nomeArquivo = arquivo.getNomeArquivo() != null && !arquivo.getNomeArquivo().isEmpty()
                ? arquivo.getNomeArquivo()
                : "livro-" + id + ".pdf";

        return new ArquivoLivro(nomeArquivo, tipoConteudo, conteudo);
    }

    public ArquivoLivro baixarCapa(UUID id) {
        LivroCapaEntity capa = livroCapaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Este livro ainda nao possui uma capa armazenada."));
        String nomeCapa = capa.getNomeCapa() != null && !capa.getNomeCapa().isEmpty()
                ? capa.getNomeCapa()
                : "capa-" + id;
        String tipoConteudo = capa.getTipoConteudo() != null && !capa.getTipoConteudo().isEmpty()
                ? capa.getTipoConteudo()
                : "application/octet-stream";
        return new ArquivoLivro(nomeCapa, tipoConteudo, capa.getArquivoCapa());
    }

    private LivroResponse paraResponse(LivroConsultaProjection livro) {
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
                livro.getPossuiArquivo(),
                livro.getNomeArquivo(),
                livro.getTipoConteudo(),
                livro.getTamanhoBytes(),
                livro.getPossuiCapa(),
                livro.getNomeCapa(),
                livro.getTipoCapa(),
                livro.getTamanhoCapaBytes(),
                livro.getCriadoEm(),
                livro.getAtualizadoEm()
        );
    }

    public record ArquivoLivro(String nomeArquivo, String tipoConteudo, byte[] conteudo) {
    }
}
