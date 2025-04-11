package com.esiitech.publication_memoire.service.implementations;

import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.Role;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Random;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final EmailService emailService;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, EmailService emailService) {
        this.utilisateurRepository = utilisateurRepository;
        this.emailService = emailService;
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

}
