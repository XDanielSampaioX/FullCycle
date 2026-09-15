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

/** Imagem de capa do livro, mantida fora dos metadados e do PDF. */
@Entity
@Table(name = "tb_livro_capa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LivroCapaEntity {

    @Id
    @Column(name = "livro_id")
    private UUID livroId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "livro_id", nullable = false)
    private LivroEntity livro;

    @Column(name = "nome_capa", length = 255)
    private String nomeCapa;

    @Column(name = "tipo_conteudo", length = 100, nullable = false)
    private String tipoConteudo;

    @Column(name = "tamanho_bytes", nullable = false)
    private Long tamanhoBytes;

    @Column(name = "hash_capa", unique = true, length = 64)
    private String hashCapa;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "arquivo_capa", columnDefinition = "bytea", nullable = false)
    private byte[] arquivoCapa;
}
