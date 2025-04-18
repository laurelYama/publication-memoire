package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.ActivationRequest;
import com.esiitech.publication_memoire.dto.ChangePasswordRequest;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import com.esiitech.publication_memoire.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/utilisateurs")
@Tag(name = "Utilisateur", description = "Gestion des utilisateurs (activation de compte, changement de mot de passe, etc.)")
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder,
                                 UtilisateurService utilisateurService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/activer-compte/{token}")
    @Operation(
            summary = "Activer un compte utilisateur",
            description = "Permet à l'utilisateur de définir un mot de passe et d'activer son compte via un lien d'activation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compte activé avec succès"),
            @ApiResponse(responseCode = "400", description = "Erreur de validation ou lien invalide",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<String> creerMotDePasseEtActiverCompte(
            @PathVariable String token,
            @RequestBody ActivationRequest request) {

        String motDePasse = request.getMotDePasse();
        String confirmationMotDePasse = request.getConfirmationMotDePasse();

        if (!motDePasse.equals(confirmationMotDePasse)) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas.");
        }

        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByActivationToken(token);
        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Lien invalide ou déjà utilisé.");
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        if (utilisateur.getTokenExpiration().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Lien expiré. Veuillez demander un nouveau lien.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasse));
        utilisateur.setActif(true);
        utilisateur.setPasswordCreated(true);
        utilisateur.setActivationToken(null);
        utilisateur.setTokenExpiration(null);

        utilisateurRepository.save(utilisateur);

        return ResponseEntity.ok("Mot de passe créé et compte activé avec succès !");
    }

    @PutMapping("/changer-mot-de-passe")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Changer le mot de passe",
            description = "Permet à un utilisateur authentifié de changer son mot de passe."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mot de passe changé avec succès"),
            @ApiResponse(responseCode = "400", description = "Erreur de validation du mot de passe"),
            @ApiResponse(responseCode = "401", description = "Non autorisé")
    })
    public ResponseEntity<?> changerMotDePasse(@RequestBody ChangePasswordRequest request,
                                               @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return utilisateurService.changerMotDePasse(utilisateur, request);
    }
}
