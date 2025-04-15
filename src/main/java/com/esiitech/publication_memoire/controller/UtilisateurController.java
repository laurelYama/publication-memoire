package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.ActivationRequest;
import com.esiitech.publication_memoire.dto.ChangePasswordRequest;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import com.esiitech.publication_memoire.service.UtilisateurService;
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
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder, UtilisateurService utilisateurService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.utilisateurService = utilisateurService;
    }

    /**
     * Méthode pour activer le compte de l'utilisateur en créant un mot de passe et validant l'activation du compte.
     *
     * @param token Le token d'activation fourni à l'utilisateur.
     * @param request Contient les informations de mot de passe.
     * @return Une réponse indiquant le succès ou l'échec de l'activation.
     */
    @PostMapping("/activer-compte/{token}")
    public ResponseEntity<String> creerMotDePasseEtActiverCompte(
            @PathVariable String token,
            @RequestBody ActivationRequest request) {

        // Vérification de la correspondance des mots de passe
        String motDePasse = request.getMotDePasse();
        String confirmationMotDePasse = request.getConfirmationMotDePasse();

        if (!motDePasse.equals(confirmationMotDePasse)) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas.");
        }

        // Recherche de l'utilisateur par token d'activation
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByActivationToken(token);
        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Lien invalide ou déjà utilisé.");
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        // Vérification de l'expiration du token
        if (utilisateur.getTokenExpiration().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Lien expiré. Veuillez demander un nouveau lien.");
        }

        // Mise à jour des informations de l'utilisateur et activation du compte
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasse));
        utilisateur.setActif(true);
        utilisateur.setPasswordCreated(true);
        utilisateur.setActivationToken(null);
        utilisateur.setTokenExpiration(null);

        utilisateurRepository.save(utilisateur);

        return ResponseEntity.ok("Mot de passe créé et compte activé avec succès !");
    }

    /**
     * Méthode permettant de changer le mot de passe d'un utilisateur connecté.
     * L'utilisateur doit être authentifié pour accéder à cette fonctionnalité.
     *
     * @param request Contient les informations du changement de mot de passe.
     * @param userDetails Détails de l'utilisateur authentifié.
     * @return Une réponse indiquant le succès ou l'échec du changement de mot de passe.
     */
    @PutMapping("/changer-mot-de-passe")
    @PreAuthorize("isAuthenticated()")  // Permet l'accès uniquement aux utilisateurs authentifiés
    public ResponseEntity<?> changerMotDePasse(@RequestBody ChangePasswordRequest request,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        // Recherche de l'utilisateur par son email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Appel du service pour changer le mot de passe
        return utilisateurService.changerMotDePasse(utilisateur, request);
    }
}
