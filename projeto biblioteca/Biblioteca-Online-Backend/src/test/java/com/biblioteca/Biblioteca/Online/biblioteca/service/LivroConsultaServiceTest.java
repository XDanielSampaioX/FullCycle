package com.biblioteca.Biblioteca.Online.biblioteca.service;

import com.biblioteca.Biblioteca.Online.acervo.repository.LivroArquivoRepository;
import com.biblioteca.Biblioteca.Online.acervo.repository.LivroCapaRepository;
import com.biblioteca.Biblioteca.Online.biblioteca.dto.LivroResponse;
import com.biblioteca.Biblioteca.Online.biblioteca.repository.LivroConsultaProjection;
import com.biblioteca.Biblioteca.Online.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LivroConsultaServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private LivroArquivoRepository livroArquivoRepository;

    @Mock
    private LivroCapaRepository livroCapaRepository;

    @InjectMocks
    private LivroConsultaService livroConsultaService;

    @Test
    void deveConsultarLivrosComPaginacaoETermoSemAlterarTermo() {
        LivroConsultaProjection livro = projection("Java para iniciantes");
        when(livroRepository.buscarPorTermo(eq(" java "), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(livro), PageRequest.of(1, 10), 11));

        Page<LivroResponse> resposta = livroConsultaService.buscar(" java ", PageRequest.of(1, 10));

        assertThat(resposta.getContent()).hasSize(1);
        assertThat(resposta.getContent().getFirst().titulo()).isEqualTo("Java para iniciantes");
        assertThat(resposta.getContent().getFirst().possuiArquivo()).isTrue();
        assertThat(resposta.getContent().getFirst().possuiCapa()).isTrue();
        assertThat(resposta.getNumber()).isEqualTo(1);
        assertThat(resposta.getTotalElements()).isEqualTo(11);
        verify(livroRepository).buscarPorTermo(eq(" java "), any(Pageable.class));
    }

    @Test
    void deveRecusarDownloadQuandoLivroNaoTemArquivo() {
        UUID id = UUID.randomUUID();
        when(livroArquivoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> livroConsultaService.baixarArquivo(id))
                .isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessage("Este livro ainda nao possui um PDF armazenado.");
    }

    private LivroConsultaProjection projection(String titulo) {
        UUID id = UUID.randomUUID();
        return new LivroConsultaProjection() {
            public UUID getId() { return id; }
            public String getTitulo() { return titulo; }
            public String getAutores() { return "Autor"; }
            public String getDescricao() { return "Descricao"; }
            public String getCategoria() { return "Programacao"; }
            public String getIdioma() { return "pt-BR"; }
            public String getLicenca() { return "CC BY-SA 4.0"; }
            public String getUrlLicenca() { return "https://creativecommons.org/licenses/by-sa/4.0/"; }
            public String getAtribuicao() { return "Autor. " + titulo; }
            public String getUrlOrigem() { return "https://example.org/" + titulo; }
            public String getUrlLeitura() { return null; }
            public String getUrlDownload() { return null; }
            public Instant getCriadoEm() { return null; }
            public Instant getAtualizadoEm() { return null; }
            public boolean getPossuiArquivo() { return true; }
            public String getNomeArquivo() { return "livro.pdf"; }
            public String getTipoConteudo() { return "application/pdf"; }
            public Long getTamanhoBytes() { return 5L; }
            public boolean getPossuiCapa() { return true; }
            public String getNomeCapa() { return "capa.jpg"; }
            public String getTipoCapa() { return "image/jpeg"; }
            public Long getTamanhoCapaBytes() { return 5L; }
        };
    }
}
