package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.AuthRequestDto;
import com.esiitech.publication_memoire.dto.AuthResponseDto;
import com.esiitech.publication_memoire.entity.User;
import com.esiitech.publication_memoire.enums.Role;
import com.esiitech.publication_memoire.repository.UserRepository;
import com.esiitech.publication_memoire.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody AuthRequestDto request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String token = jwtUtil.generateToken(user.getEmail());

        AuthResponseDto response = new AuthResponseDto();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setNom(user.getNom());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return response;
    }
}
