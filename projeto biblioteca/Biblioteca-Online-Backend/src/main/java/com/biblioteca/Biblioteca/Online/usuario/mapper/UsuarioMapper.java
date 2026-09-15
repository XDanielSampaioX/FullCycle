package com.biblioteca.Biblioteca.Online.usuario.mapper;

import com.biblioteca.Biblioteca.Online.usuario.domain.Endereco;
import com.biblioteca.Biblioteca.Online.usuario.domain.UsuarioEntity;
import com.biblioteca.Biblioteca.Online.usuario.dto.UsuarioRequest;
import com.biblioteca.Biblioteca.Online.usuario.dto.UsuarioResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = EnderecoMapper.class)
public interface UsuarioMapper {

    UsuarioEntity toEntity(UsuarioRequest request);

    UsuarioResponse toResponse(UsuarioEntity usuario);

    List<UsuarioResponse> toResponseList(List<UsuarioEntity> usuarios);

    default UsuarioEntity toEntity(UsuarioRequest request, Endereco endereco) {
        if (request == null) {
            return null;
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNome(request.nome());
        usuario.setCpf(request.cpf());
        usuario.setEndereco(endereco);

        return usuario;
    }
}
