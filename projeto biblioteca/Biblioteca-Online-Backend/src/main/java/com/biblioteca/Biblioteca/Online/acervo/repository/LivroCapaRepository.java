package com.biblioteca.Biblioteca.Online.acervo.repository;

import com.biblioteca.Biblioteca.Online.acervo.domain.LivroCapaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LivroCapaRepository extends JpaRepository<LivroCapaEntity, UUID> {

    Optional<LivroCapaEntity> findByHashCapa(String hashCapa);
}
