package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.ForgotPasswordRequest;
import com.esiitech.publication_memoire.dto.LoginRequest;
import com.esiitech.publication_memoire.config.JwtService;
import com.esiitech.publication_memoire.dto.ResetPasswordRequest;
import com.esiitech.publication_memoire.service.implementations.CustomUserDetailsService;
import com.esiitech.publication_memoire.service.implementations.UtilisateurService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/auth/")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final UtilisateurService utilisateurService;

    // Injecte CustomUserDetailsService dans le contrôleur
    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService customUserDetailsService,
                          JwtService jwtService, UtilisateurService utilisateurService) {
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtService = jwtService;
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Authentifier l'utilisateur
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getMotDePasse())
            );

            // Charger l'utilisateur et générer le token
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginRequest.getEmail());
            String token = jwtService.genererToken(userDetails);

            return ResponseEntity.ok(Map.of(
                    "message", "Connexion réussie",
                    "token", token
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Identifiants invalides."));
        }
    }

    @PostMapping("/mot-de-passe-oublie")
    public ResponseEntity<?> motDePasseOublie(@RequestBody ForgotPasswordRequest request) {
        return utilisateurService.envoyerLienDeReinitialisation(request.getEmail());
    }

    @PostMapping("/reinitialiser-mot-de-passe/{token}")
    public ResponseEntity<?> reinitialiserMotDePasse(@PathVariable String token,
                                                     @RequestBody ResetPasswordRequest request) {
        return utilisateurService.reinitialiserMotDePasse(token, request);
    }

}
