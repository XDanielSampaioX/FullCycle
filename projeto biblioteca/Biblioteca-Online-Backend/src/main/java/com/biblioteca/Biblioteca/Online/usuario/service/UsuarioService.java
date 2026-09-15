package com.biblioteca.Biblioteca.Online.usuario.service;

import com.biblioteca.Biblioteca.Online.usuario.domain.Endereco;
import com.biblioteca.Biblioteca.Online.usuario.domain.UsuarioEntity;
import com.biblioteca.Biblioteca.Online.usuario.dto.UsuarioRequest;
import com.biblioteca.Biblioteca.Online.usuario.dto.UsuarioResponse;
import com.biblioteca.Biblioteca.Online.usuario.mapper.EnderecoMapper;
import com.biblioteca.Biblioteca.Online.usuario.mapper.UsuarioMapper;
import com.biblioteca.Biblioteca.Online.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final EnderecoMapper enderecoMapper;
    private final EnderecoService enderecoService;

    public UsuarioResponse cadastrar(UsuarioRequest request) {
        if (usuarioRepository.existsByCpf(request.cpf())) {
            throw new IllegalArgumentException("CPF ja cadastrado.");
        }

        Endereco endereco = enderecoService.buscarPorCep(request.endereco().cep());
        enderecoMapper.merge(endereco, request.endereco());

        UsuarioEntity usuario = usuarioMapper.toEntity(request, endereco);
        UsuarioEntity usuarioSalvo = usuarioRepository.save(usuario);

        return usuarioMapper.toResponse(usuarioSalvo);
    }

    public List<UsuarioResponse> listar() {
        return usuarioMapper.toResponseList(usuarioRepository.findAll());
    }
}
