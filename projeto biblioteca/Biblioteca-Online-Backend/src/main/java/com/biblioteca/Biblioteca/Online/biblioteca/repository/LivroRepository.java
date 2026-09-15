package com.biblioteca.Biblioteca.Online.biblioteca.repository;

import com.biblioteca.Biblioteca.Online.acervo.domain.LivroArquivoEntity;
import com.biblioteca.Biblioteca.Online.acervo.domain.LivroCapaEntity;
import com.biblioteca.Biblioteca.Online.biblioteca.domain.LivroEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LivroRepository extends JpaRepository<LivroEntity, UUID> {

    Optional<LivroEntity> findByTituloAndUrlOrigem(String titulo, String urlOrigem);

    @Query(value = """
            select l.id as id,
                   l.titulo as titulo,
                   l.autores as autores,
                   l.descricao as descricao,
                   l.categoria as categoria,
                   l.idioma as idioma,
                   l.licenca as licenca,
                   l.urlLicenca as urlLicenca,
                   l.atribuicao as atribuicao,
                   l.urlOrigem as urlOrigem,
                   l.urlLeitura as urlLeitura,
                   l.urlDownload as urlDownload,
                   l.criadoEm as criadoEm,
                   l.atualizadoEm as atualizadoEm,
                   case when a.livroId is not null then true else false end as possuiArquivo,
                   a.nomeArquivo as nomeArquivo,
                   a.tipoConteudo as tipoConteudo,
                   a.tamanhoBytes as tamanhoBytes,
                   case when c.livroId is not null then true else false end as possuiCapa,
                   c.nomeCapa as nomeCapa,
                   c.tipoConteudo as tipoCapa,
                   c.tamanhoBytes as tamanhoCapaBytes
              from LivroEntity l
              left join LivroArquivoEntity a on a.livro = l
              left join LivroCapaEntity c on c.livro = l
            """,
            countQuery = "select count(l) from LivroEntity l")
    Page<LivroConsultaProjection> findAllBy(Pageable pageable);

    @Query(value = """
            select l.id as id,
                   l.titulo as titulo,
                   l.autores as autores,
                   l.descricao as descricao,
                   l.categoria as categoria,
                   l.idioma as idioma,
                   l.licenca as licenca,
                   l.urlLicenca as urlLicenca,
                   l.atribuicao as atribuicao,
                   l.urlOrigem as urlOrigem,
                   l.urlLeitura as urlLeitura,
                   l.urlDownload as urlDownload,
                   l.criadoEm as criadoEm,
                   l.atualizadoEm as atualizadoEm,
                   case when a.livroId is not null then true else false end as possuiArquivo,
                   a.nomeArquivo as nomeArquivo,
                   a.tipoConteudo as tipoConteudo,
                   a.tamanhoBytes as tamanhoBytes,
                   case when c.livroId is not null then true else false end as possuiCapa,
                   c.nomeCapa as nomeCapa,
                   c.tipoConteudo as tipoCapa,
                   c.tamanhoBytes as tamanhoCapaBytes
              from LivroEntity l
              left join LivroArquivoEntity a on a.livro = l
              left join LivroCapaEntity c on c.livro = l
             where lower(l.titulo) like lower(concat('%', :termo, '%'))
                or lower(l.autores) like lower(concat('%', :termo, '%'))
                or lower(l.categoria) like lower(concat('%', :termo, '%'))
            """,
            countQuery = """
                    select count(l)
                      from LivroEntity l
                     where lower(l.titulo) like lower(concat('%', :termo, '%'))
                        or lower(l.autores) like lower(concat('%', :termo, '%'))
                        or lower(l.categoria) like lower(concat('%', :termo, '%'))
                    """)
    Page<LivroConsultaProjection> buscarPorTermo(@Param("termo") String termo, Pageable pageable);

    @Query("""
            select l.id as id,
                   l.titulo as titulo,
                   l.autores as autores,
                   l.descricao as descricao,
                   l.categoria as categoria,
                   l.idioma as idioma,
                   l.licenca as licenca,
                   l.urlLicenca as urlLicenca,
                   l.atribuicao as atribuicao,
                   l.urlOrigem as urlOrigem,
                   l.urlLeitura as urlLeitura,
                   l.urlDownload as urlDownload,
                   l.criadoEm as criadoEm,
                   l.atualizadoEm as atualizadoEm,
                   case when a.livroId is not null then true else false end as possuiArquivo,
                   a.nomeArquivo as nomeArquivo,
                   a.tipoConteudo as tipoConteudo,
                   a.tamanhoBytes as tamanhoBytes,
                   case when c.livroId is not null then true else false end as possuiCapa,
                   c.nomeCapa as nomeCapa,
                   c.tipoConteudo as tipoCapa,
                   c.tamanhoBytes as tamanhoCapaBytes
              from LivroEntity l
              left join LivroArquivoEntity a on a.livro = l
              left join LivroCapaEntity c on c.livro = l
             where l.id = :id
            """)
    Optional<LivroConsultaProjection> buscarProjecaoPorId(@Param("id") UUID id);
}
