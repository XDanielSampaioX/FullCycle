package com.biblioteca.Biblioteca.Online.acervo.domain;

import com.biblioteca.Biblioteca.Online.biblioteca.domain.LivroEntity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Dados binarios e metadados do PDF, separados da entidade de catalogo. */
@Entity
@Table(name = "tb_livro_arquivo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LivroArquivoEntity {

    @Id
    @Column(name = "livro_id")
    private UUID livroId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "livro_id", nullable = false)
    private LivroEntity livro;

    @Column(name = "nome_arquivo", length = 255)
    private String nomeArquivo;

    @Column(name = "tipo_conteudo", length = 100)
    private String tipoConteudo;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @Column(name = "hash_arquivo", unique = true, length = 64)
    private String hashArquivo;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "arquivo_pdf", columnDefinition = "bytea")
    private byte[] arquivoPdf;
}
