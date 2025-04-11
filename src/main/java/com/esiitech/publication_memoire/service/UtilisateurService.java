package com.esiitech.publication_memoire.service;

import com.esiitech.publication_memoire.Enum.Role;
import com.esiitech.publication_memoire.entity.Utilisateur;
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

    public Utilisateur creerLecteur(String nom, String prenom, String email) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(email)) {
            throw new RuntimeException("L'email est déjà utilisé !");
        }

        String token = UUID.randomUUID().toString();
        String temporaryPassword = generateTemporaryPassword(); // Mot de passe temporaire généré
        Utilisateur lecteur = new Utilisateur();
        lecteur.setNom(nom);
        lecteur.setPrenom(prenom);
        lecteur.setEmail(email);
        lecteur.setRole(Role.LECTEUR);
        lecteur.setActivationToken(token);
        lecteur.setMotDePasse(temporaryPassword); // On enregistre le mot de passe temporaire
        lecteur.setTokenExpiration(LocalDateTime.now().plusHours(24));
        lecteur.setActif(false); // Le compte n'est pas encore actif
        lecteur.setPasswordCreated(false); // Le mot de passe n'a pas encore été modifié

        utilisateurRepository.save(lecteur);

        // Envoi de l'email avec le mot de passe temporaire
        emailService.sendActivationEmail(email, token);

        return lecteur;
    }
}
