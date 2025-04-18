package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.*;
import com.esiitech.publication_memoire.config.JwtService;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.service.CustomUserDetailsService;
import com.esiitech.publication_memoire.service.TrombinoscopeService;
import com.esiitech.publication_memoire.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/auth/")
@Tag(name = "Authentification", description = "Endpoints liés à l'authentification et la gestion des utilisateurs")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final UtilisateurService utilisateurService;
    private final TrombinoscopeService trombinoscopeService;

    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService customUserDetailsService,
                          JwtService jwtService,
                          UtilisateurService utilisateurService,
                          TrombinoscopeService trombinoscopeService) {
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtService = jwtService;
        this.utilisateurService = utilisateurService;
        this.trombinoscopeService = trombinoscopeService;
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connexion réussie"),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides"),
            @ApiResponse(responseCode = "403", description = "Compte désactivé")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getMotDePasse())
            );
            Utilisateur utilisateur = utilisateurService.getByEmail(loginRequest.getEmail());

            if (!utilisateur.isActif()) {
                return ResponseEntity.status(403).body(Map.of("message", "Compte désactivé."));
            }

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
    @Operation(summary = "Mot de passe oublié", description = "Envoie un lien de réinitialisation à l'adresse e-mail fournie.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lien envoyé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<?> motDePasseOublie(@RequestBody ForgotPasswordRequest request) {
        return utilisateurService.envoyerLienDeReinitialisation(request.getEmail());
    }

    @PostMapping("/reinitialiser-mot-de-passe/{token}")
    @Operation(summary = "Réinitialiser mot de passe", description = "Permet de réinitialiser le mot de passe à l'aide d'un token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé avec succès"),
            @ApiResponse(responseCode = "400", description = "Token invalide ou expiré")
    })
    public ResponseEntity<?> reinitialiserMotDePasse(
            @Parameter(description = "Token de réinitialisation") @PathVariable String token,
            @RequestBody ResetPasswordRequest request) {
        return utilisateurService.reinitialiserMotDePasse(token, request);
    }

    @GetMapping("/me")
    @Operation(summary = "Mon profil", description = "Retourne les informations de l'utilisateur connecté.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "401", description = "Token invalide ou expiré")
    })
    public ResponseEntity<?> getMonProfil(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Utilisateur utilisateur = utilisateurService.getByEmail(userDetails.getUsername());

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

    @GetMapping("/verification")
    @Operation(summary = "Vérifier un étudiant", description = "Recherche un étudiant par identifiant (email ou matricule).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Étudiant trouvé"),
            @ApiResponse(responseCode = "404", description = "Étudiant non trouvé")
    })
    public ResponseEntity<EtudiantDto> verifierEtudiant(
            @RequestParam @Parameter(description = "Email ou matricule de l'étudiant") String identifiant) {
        try {
            EtudiantDto etudiant = trombinoscopeService.chercherEtudiant(identifiant);
            return ResponseEntity.ok(etudiant);
        } catch (Exception e) {
            // Gérer les exceptions et renvoyer une réponse "404 Not Found"
            return ResponseEntity.notFound().build();
        }
    }

}
