package com.biblioteca.Biblioteca.Online.acervo.repository;

import com.biblioteca.Biblioteca.Online.acervo.domain.LivroArquivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LivroArquivoRepository extends JpaRepository<LivroArquivoEntity, UUID> {

    Optional<LivroArquivoEntity> findByHashArquivo(String hashArquivo);
}
