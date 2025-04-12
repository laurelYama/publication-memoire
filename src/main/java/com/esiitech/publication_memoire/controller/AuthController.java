package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.ForgotPasswordRequest;
import com.esiitech.publication_memoire.dto.LoginRequest;
import com.esiitech.publication_memoire.config.JwtService;
import com.esiitech.publication_memoire.dto.ResetPasswordRequest;
import com.esiitech.publication_memoire.dto.UtilisateurDTO;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.service.CustomUserDetailsService;
import com.esiitech.publication_memoire.service.UtilisateurService;
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
            Utilisateur utilisateur = utilisateurService.getByEmail(loginRequest.getEmail());

            if (!utilisateur.isActif()) {
                return ResponseEntity.status(403).body(Map.of("message", "Compte désactivé."));
            }

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

    @GetMapping("/me")
    public ResponseEntity<?> getMonProfil(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String email = jwtService.extraireNomUtilisateur(token);

            Utilisateur utilisateur = utilisateurService.getByEmail(email);

            UtilisateurDTO dto = new UtilisateurDTO();
            dto.setNom(utilisateur.getNom());
            dto.setPrenom(utilisateur.getPrenom());
            dto.setEmail(utilisateur.getEmail());
            dto.setRole(utilisateur.getRole().name());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Token invalide ou expiré."));
        }
    }


}
