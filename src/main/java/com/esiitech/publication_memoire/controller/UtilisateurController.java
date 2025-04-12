package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.ActivationRequest;
import com.esiitech.publication_memoire.dto.ChangePasswordRequest;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import com.esiitech.publication_memoire.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/activer-compte/{token}")
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
    public ResponseEntity<?> changerMotDePasse(@RequestBody ChangePasswordRequest request,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (utilisateur == null) return ResponseEntity.status(404).body("Utilisateur non trouvé.");
        return utilisateurService.changerMotDePasse(utilisateur, request);
    }

}
