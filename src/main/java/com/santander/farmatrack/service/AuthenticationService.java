package com.santander.farmatrack.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.santander.farmatrack.entity.Usuario;
import com.santander.farmatrack.repository.UsuarioRepository;
import com.santander.farmatrack.util.JwtResponse;
import com.santander.farmatrack.util.LoginRequest;
import com.santander.farmatrack.util.RefreshTokenRequest;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        var user = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return new JwtResponse(jwtToken, refreshToken);
    }

    public JwtResponse refreshToken(RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.refreshToken());
        if (username != null) {
            var user = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (jwtService.isTokenValid(request.refreshToken(), user)) {
                var accessToken = jwtService.generateToken(user);
                return new JwtResponse(accessToken, request.refreshToken());
            }
        }
        throw new RuntimeException("Token de refresco inválido");
    }
}