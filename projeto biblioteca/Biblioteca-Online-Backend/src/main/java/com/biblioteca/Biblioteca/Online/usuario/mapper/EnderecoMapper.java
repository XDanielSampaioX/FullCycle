package com.biblioteca.Biblioteca.Online.usuario.mapper;

import com.biblioteca.Biblioteca.Online.usuario.domain.Endereco;
import com.biblioteca.Biblioteca.Online.usuario.dto.EnderecoRequest;
import com.biblioteca.Biblioteca.Online.usuario.dto.EnderecoResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EnderecoMapper {

    Endereco toEntity(EnderecoRequest request);

    EnderecoResponse toResponse(Endereco endereco);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(EnderecoRequest request, @MappingTarget Endereco endereco);

    default Endereco merge(Endereco endereco, EnderecoRequest request) {
        if (endereco == null) {
            return toEntity(request);
        }

        if (request == null) {
            return endereco;
        }

        updateFromRequest(request, endereco);

        return endereco;
    }
}
