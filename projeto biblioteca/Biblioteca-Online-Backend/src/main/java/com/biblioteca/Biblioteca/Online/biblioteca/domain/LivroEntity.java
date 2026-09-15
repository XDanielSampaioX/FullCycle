package com.biblioteca.Biblioteca.Online.biblioteca.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "tb_livro",
        indexes = @Index(name = "idx_tb_livro_titulo", columnList = "titulo"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tb_livro_titulo_origem",
                columnNames = {"titulo", "url_origem"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LivroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 250)
    private String titulo;

    @Column(nullable = false, length = 250)
    private String autores;

    @Column(length = 4_000)
    private String descricao;

    @Column(nullable = false, length = 120)
    private String categoria;

    @Column(nullable = false, length = 20)
    private String idioma;

    @Column(nullable = false, length = 80)
    private String licenca;

    @Column(name = "url_licenca", nullable = false, length = 500)
    private String urlLicenca;

    @Column(nullable = false, length = 2_000)
    private String atribuicao;

    @Column(name = "url_origem", nullable = false, length = 1_000)
    private String urlOrigem;

    @Column(name = "url_leitura", length = 1_000)
    private String urlLeitura;

    @Column(name = "url_download", length = 1_000)
    private String urlDownload;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = Instant.now();
    }
}
