package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.EtudiantDto;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.service.TrombinoscopeService;
import com.esiitech.publication_memoire.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Profil", description = "Endpoints pour gérer le profil de l'etudiant connecté")
public class ProfilController {

    private final TrombinoscopeService trombinoscopeService;
    private final UtilisateurService utilisateurService;

    public ProfilController(TrombinoscopeService trombinoscopeService,
                            UtilisateurService utilisateurService) {
        this.trombinoscopeService = trombinoscopeService;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/mon-profil")
    @Operation(summary = "Obtenir le profil de l'utilisateur connecté",
            description = "Récupère les informations du profil de l'utilisateur connecté (étudiant)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil trouvé avec succès"),
            @ApiResponse(responseCode = "404", description = "Profil non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<EtudiantDto> getMonProfil() {
        try {
            // Récupérer l'utilisateur connecté
            Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();

            if (utilisateur == null) {
                // Retourner une réponse 401 Unauthorized si aucun utilisateur n'est connecté
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            // Appeler le service pour rechercher les détails de l'étudiant à partir de son email
            EtudiantDto etudiant = trombinoscopeService.chercherEtudiant(utilisateur.getEmail());

            // Retourner une réponse 200 OK avec les détails de l'étudiant
            return ResponseEntity.ok(etudiant);

        } catch (Exception e) {
            // Gérer les erreurs en journalisant et retourner une réponse 404 Not Found
            System.err.println("Erreur lors de la récupération du profil : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}
