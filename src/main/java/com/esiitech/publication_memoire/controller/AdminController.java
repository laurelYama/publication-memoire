package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.UtilisateurDTO;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import com.esiitech.publication_memoire.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UtilisateurService utilisateurService;
    private final UtilisateurRepository utilisateurRepository;

    public AdminController(UtilisateurService utilisateurService, UtilisateurRepository utilisateurRepository) {
        this.utilisateurService = utilisateurService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @PostMapping("/lecteurs")
    public ResponseEntity<?> ajouterLecteur(@RequestBody UtilisateurDTO request) {
        if (request.getNom() == null || request.getPrenom() == null || request.getEmail() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tous les champs sont requis."));
        }

        // Vérifier si l'email est déjà utilisé
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cet email est déjà utilisé."));
        }

        Utilisateur lecteur = utilisateurService.creerLecteur(request.getNom(), request.getPrenom(), request.getEmail());

        return ResponseEntity.ok(Map.of("message", "Lecteur créé avec succès. Email d'activation envoyé."));
    }


}

