package com.biblioteca.Biblioteca.Online.usuario.repository;

import com.biblioteca.Biblioteca.Online.usuario.domain.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {

    boolean existsByCpf(String cpf);

    Optional<UsuarioEntity> findByCpf(String cpf);
}
