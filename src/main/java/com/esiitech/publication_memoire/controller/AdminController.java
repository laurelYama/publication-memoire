package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.UtilisateurDTO;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.Role;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import com.esiitech.publication_memoire.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UtilisateurService utilisateurService;
    private final UtilisateurRepository utilisateurRepository;

    public AdminController(UtilisateurService utilisateurService, UtilisateurRepository utilisateurRepository) {
        this.utilisateurService = utilisateurService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @PostMapping("/utilisateurs")
    public ResponseEntity<?> ajouterUtilisateur(@RequestBody UtilisateurDTO request) {
        if (request.getNom() == null || request.getPrenom() == null || request.getEmail() == null || request.getRole() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tous les champs sont requis, y compris le rôle."));
        }

        // Vérifier si l'email est déjà utilisé
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cet email est déjà utilisé."));
        }

        try {
            Role role = Role.valueOf(request.getRole().toUpperCase()); // Convertit le String en enum
            Utilisateur utilisateur = utilisateurService.creerUtilisateur(
                    request.getNom(), request.getPrenom(), request.getEmail(), role
            );

            return ResponseEntity.ok(Map.of("message", "Utilisateur créé avec succès. Email d'activation envoyé."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le rôle fourni est invalide."));
        }
    }


    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles() {
        List<String> roles = Arrays.stream(Role.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    @PutMapping("/utilisateur/{id}/activer")
    public ResponseEntity<?> activerUtilisateur(@PathVariable Long id) {
        return utilisateurService.activerUtilisateur(id, true);
    }

    @PutMapping("/utilisateur/{id}/desactiver")
    public ResponseEntity<?> desactiverUtilisateur(@PathVariable Long id) {
        return utilisateurService.activerUtilisateur(id, false);
    }



}

