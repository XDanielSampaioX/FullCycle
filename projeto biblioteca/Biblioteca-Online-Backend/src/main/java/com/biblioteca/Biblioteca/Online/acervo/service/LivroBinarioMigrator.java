package com.biblioteca.Biblioteca.Online.acervo.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Copia os PDFs antigos, que ficavam em tb_livro, para a nova tabela de
 * arquivos sem transportar os bytes pela aplicacao.
 */
@Component
@RequiredArgsConstructor
public class LivroBinarioMigrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LivroBinarioMigrator.class);

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrarArquivosLegados() {
        if (!colunaExiste("arquivo_pdf")) {
            return;
        }

        jdbcTemplate.execute("""
                create table if not exists tb_livro_arquivo (
                    livro_id uuid primary key references tb_livro(id) on delete cascade,
                    nome_arquivo varchar(255),
                    tipo_conteudo varchar(100),
                    tamanho_bytes bigint,
                    hash_arquivo varchar(64) unique,
                    arquivo_pdf bytea
                )
                """);

        int migrados = jdbcTemplate.update("""
                insert into tb_livro_arquivo (
                    livro_id, nome_arquivo, tipo_conteudo, tamanho_bytes, hash_arquivo, arquivo_pdf
                )
                select l.id, l.nome_arquivo, l.tipo_conteudo, l.tamanho_bytes,
                       l.hash_arquivo, l.arquivo_pdf
                  from tb_livro l
                 where l.arquivo_pdf is not null
                   and not exists (
                       select 1 from tb_livro_arquivo a where a.livro_id = l.id
                   )
                """);

        if (migrados > 0) {
            LOGGER.info("{} arquivo(s) legado(s) migrado(s) para tb_livro_arquivo.", migrados);
        }
    }

    private boolean colunaExiste(String nomeColuna) {
        Boolean existe = jdbcTemplate.queryForObject("""
                select exists (
                    select 1
                      from information_schema.columns
                     where table_schema = current_schema()
                       and table_name = 'tb_livro'
                       and column_name = ?
                )
                """, Boolean.class, nomeColuna);
        return Boolean.TRUE.equals(existe);
    }
}
