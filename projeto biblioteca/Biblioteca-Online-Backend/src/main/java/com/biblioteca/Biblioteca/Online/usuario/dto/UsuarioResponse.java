package com.biblioteca.Biblioteca.Online.usuario.dto;

import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String cpf,
        EnderecoResponse endereco
) {
}
