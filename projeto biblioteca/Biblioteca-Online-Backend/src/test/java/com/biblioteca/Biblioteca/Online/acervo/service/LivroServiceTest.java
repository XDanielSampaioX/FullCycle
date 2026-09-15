package com.biblioteca.Biblioteca.Online.acervo.service;

import com.biblioteca.Biblioteca.Online.biblioteca.domain.LivroEntity;
import com.biblioteca.Biblioteca.Online.acervo.domain.LivroArquivoEntity;
import com.biblioteca.Biblioteca.Online.acervo.domain.LivroCapaEntity;
import com.biblioteca.Biblioteca.Online.acervo.dto.LivroCadastroRequest;
import com.biblioteca.Biblioteca.Online.acervo.repository.LivroArquivoRepository;
import com.biblioteca.Biblioteca.Online.acervo.repository.LivroCapaRepository;
import com.biblioteca.Biblioteca.Online.biblioteca.dto.LivroResponse;
import com.biblioteca.Biblioteca.Online.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private LivroArquivoRepository livroArquivoRepository;

    @Mock
    private LivroCapaRepository livroCapaRepository;

    @InjectMocks
    private LivroService livroService;

    @Test
    void deveCadastrarPdfComMetadadosEHash() {
        when(livroArquivoRepository.findByHashArquivo(anyString())).thenReturn(Optional.empty());
        when(livroArquivoRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(livroRepository.save(any(LivroEntity.class))).thenAnswer(invocation -> {
            LivroEntity livro = invocation.getArgument(0);
            livro.setId(UUID.fromString("aa3ed245-4109-4c8e-b2cb-67b8d34dc4ec"));
            return livro;
        });
        when(livroArquivoRepository.save(any(LivroArquivoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "exemplo.pdf",
                "application/pdf",
                "%PDF-1.7\nconteudo de teste".getBytes(StandardCharsets.US_ASCII)
        );

        LivroResponse response = livroService.cadastrar(novoLivro(), arquivo);

        ArgumentCaptor<LivroArquivoEntity> captor = ArgumentCaptor.forClass(LivroArquivoEntity.class);
        verify(livroArquivoRepository).save(captor.capture());

        LivroArquivoEntity salvo = captor.getValue();
        assertThat(response.id()).isEqualTo(UUID.fromString("aa3ed245-4109-4c8e-b2cb-67b8d34dc4ec"));
        assertThat(response.possuiArquivo()).isTrue();
        assertThat(salvo.getNomeArquivo()).isEqualTo("exemplo.pdf");
        assertThat(salvo.getTipoConteudo()).isEqualTo("application/pdf");
        assertThat(salvo.getHashArquivo()).hasSize(64);
        assertThat(new String(salvo.getArquivoPdf(), StandardCharsets.US_ASCII)).startsWith("%PDF-");
    }

    @Test
    void deveRecusarArquivoQueNaoEpdf() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "anotacoes.txt",
                "text/plain",
                "texto qualquer".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> livroService.cadastrar(novoLivro(), arquivo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Somente arquivos com extensao .pdf sao aceitos.");

        verify(livroRepository, never()).save(any(LivroEntity.class));
        verify(livroArquivoRepository, never()).save(any(LivroArquivoEntity.class));
    }

    @Test
    void deveSalvarCapaSeparadaDoLivro() {
        UUID id = UUID.randomUUID();
        LivroEntity livro = new LivroEntity();
        livro.setId(id);
        livro.setTitulo("Livro de capa");
        when(livroRepository.findById(id)).thenReturn(Optional.of(livro));
        when(livroCapaRepository.findByHashCapa(anyString())).thenReturn(Optional.empty());
        when(livroCapaRepository.findById(id)).thenReturn(Optional.empty());
        when(livroCapaRepository.save(any(LivroCapaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(livroArquivoRepository.findById(id)).thenReturn(Optional.empty());

        MockMultipartFile capa = new MockMultipartFile(
                "arquivo",
                "capa.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        livroService.anexarCapa(id, capa);

        verify(livroCapaRepository).save(any(LivroCapaEntity.class));
    }

    private LivroCadastroRequest novoLivro() {
        return new LivroCadastroRequest(
                "Livro de teste",
                "Autor de teste",
                "Descricao de teste",
                "Programacao",
                "pt-BR",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0/",
                "Autor de teste. Livro de teste. CC BY-SA 4.0.",
                "https://example.com/fonte",
                "https://example.com/ler",
                null
        );
    }
}
