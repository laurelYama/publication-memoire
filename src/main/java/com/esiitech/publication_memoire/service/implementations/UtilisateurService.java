package com.esiitech.publication_memoire.service.implementations;

import com.esiitech.publication_memoire.config.JwtService;
import com.esiitech.publication_memoire.dto.ChangePasswordRequest;
import com.esiitech.publication_memoire.dto.ResetPasswordRequest;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.Role;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Random;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, EmailService emailService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Génération d'un mot de passe temporaire
    private String generateTemporaryPassword() {
        // Par exemple, un mot de passe aléatoire de 8 caractères incluant des lettres et des chiffres
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            int randomIndex = random.nextInt(charset.length());
            password.append(charset.charAt(randomIndex));
        }

        return password.toString();
    }

    public Utilisateur creerUtilisateur(String nom, String prenom, String email, Role role) {
        if (utilisateurRepository.existsByEmail(email)) {
            throw new RuntimeException("L'email est déjà utilisé !");
        }

        String token = UUID.randomUUID().toString();
        String temporaryPassword = generateTemporaryPassword();

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(email);
        utilisateur.setRole(role); // Rôle choisi dynamiquement par l'admin
        utilisateur.setActivationToken(token);
        utilisateur.setMotDePasse(temporaryPassword);
        utilisateur.setTokenExpiration(LocalDateTime.now().plusHours(24));
        utilisateur.setActif(false);
        utilisateur.setPasswordCreated(false);

        utilisateurRepository.save(utilisateur);

        // Envoi de l’email avec le lien d’activation (sans le mot de passe temporaire si tu l’as retiré)
        emailService.sendActivationEmail(email, token, nom, prenom, role.name());


        return utilisateur;
    }


    public ResponseEntity<?> changerMotDePasse(Utilisateur utilisateur, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getAncienMotDePasse(), utilisateur.getMotDePasse())) {
            return ResponseEntity.badRequest().body("Ancien mot de passe incorrect.");
        }

        if (!request.getNouveauMotDePasse().equals(request.getConfirmationMotDePasse())) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);

        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }

    public ResponseEntity<?> envoyerLienDeReinitialisation(String email) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);
        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email non trouvé.");
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        String token = UUID.randomUUID().toString();
        utilisateur.setActivationToken(token);
        utilisateur.setTokenExpiration(LocalDateTime.now().plusHours(1));
        utilisateurRepository.save(utilisateur);

        String lien = "http://localhost:8080/api/auth/reinitialiser-mot-de-passe/" + token;
        emailService.sendEmail(utilisateur.getEmail(), "Réinitialisation de mot de passe", "Clique ici : " + lien);

        return ResponseEntity.ok("Email envoyé.");
    }

    public ResponseEntity<?> reinitialiserMotDePasse(String token, ResetPasswordRequest request) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByActivationToken(token);
        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Lien invalide.");
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        if (utilisateur.getTokenExpiration().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Lien expiré.");
        }

        if (!request.getNouveauMotDePasse().equals(request.getConfirmationMotDePasse())) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateur.setActivationToken(null);
        utilisateur.setTokenExpiration(null);
        utilisateur.setActif(true);
        utilisateur.setPasswordCreated(true);
        utilisateurRepository.save(utilisateur);

        return ResponseEntity.ok("Mot de passe réinitialisé.");
    }


    public ResponseEntity<?> activerUtilisateur(Long id, boolean actif) {
        Optional<Utilisateur> opt = utilisateurRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé");
        }

        Utilisateur utilisateur = opt.get();
        utilisateur.setActif(actif);
        utilisateurRepository.save(utilisateur);

        String message = actif ? "Utilisateur activé" : "Utilisateur désactivé";
        return ResponseEntity.ok(Map.of("status", "success", "message", message));
    }

    public Utilisateur getUtilisateurDepuisToken(String token) {
        String email = jwtService.extraireEmail(token);
        return getByEmail(email);
    }


    public Utilisateur getByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }




}
