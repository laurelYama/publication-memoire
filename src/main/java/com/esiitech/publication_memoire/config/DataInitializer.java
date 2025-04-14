package com.esiitech.publication_memoire.config;

import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.Role;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdminUser(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "esiitecharchives.gabon@gmail.com";

            if (!utilisateurRepository.existsByEmail(adminEmail)) {
                Utilisateur admin = new Utilisateur();
                admin.setNom("Administrateur");
                admin.setEmail(adminEmail);
                admin.setMotDePasse(passwordEncoder.encode("admin123")); // Hash du mot de passe
                admin.setRole(Role.ADMIN);
                admin.setPrenom("Admin"); // Ajouter un prénom par défaut pour l'admin

                // Si ton entité a un champ "active"
                admin.setActivationToken(null); // Pas besoin de token pour l'admin par défaut

                utilisateurRepository.save(admin);
                System.out.println("✅ Compte administrateur créé !");
            } else {
                System.out.println("ℹ️ L'administrateur existe déjà.");
            }
        };
    }
}
