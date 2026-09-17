package com.biblioteca.Biblioteca.Online.autenticacao.service;

import com.biblioteca.Biblioteca.Online.autenticacao.dto.LoginRequest;
import com.biblioteca.Biblioteca.Online.autenticacao.dto.TokenResponse;
import com.biblioteca.Biblioteca.Online.usuario.domain.UsuarioEntity;
import com.biblioteca.Biblioteca.Online.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Value("${app.security.jwt.issuer}")
    private String issuer;

    @Value("${app.security.jwt.expires-in-minutes}")
    private long expiresInMinutes;

    public TokenResponse autenticar(LoginRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByCpf(request.cpf())
                .orElseThrow(() -> new BadCredentialsException("Credenciais invalidas."));

        if (usuario.getSenha() == null || !passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new BadCredentialsException("Credenciais invalidas.");
        }

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expiresInMinutes * 60);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(usuario.getId().toString())
                .claim("cpf", usuario.getCpf())
                .claim("nome", usuario.getNome())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new TokenResponse(token, "Bearer", expiresAt.getEpochSecond() - issuedAt.getEpochSecond());
    }
}
