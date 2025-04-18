package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.HistoriqueActionDTO;
import com.esiitech.publication_memoire.entity.HistoriqueAction;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.service.HistoriqueActionService;
import com.esiitech.publication_memoire.service.UtilisateurService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des historiques d'actions des utilisateurs.
 * Permet de récupérer les actions effectuées par l'utilisateur connecté ou par un autre utilisateur (admin).
 */
@RestController
@RequestMapping("/api/historique")
@Tag(name = "Historique des actions", description = "Gestion de l’historique des actions des utilisateurs")
public class HistoriqueActionController {

    private final HistoriqueActionService historiqueService;
    private final UtilisateurService utilisateurService;

    /**
     * Constructeur avec injection des services nécessaires.
     *
     * @param historiqueService   le service de gestion des historiques
     * @param utilisateurService  le service de gestion des utilisateurs
     */
    public HistoriqueActionController(HistoriqueActionService historiqueService,
                                      UtilisateurService utilisateurService) {
        this.historiqueService = historiqueService;
        this.utilisateurService = utilisateurService;
    }

    /**
     * Récupère l'historique des actions de l'utilisateur actuellement connecté.
     *
     * @param principal l'utilisateur connecté (via Spring Security)
     * @return une liste d'objets DTO représentant l'historique de l'utilisateur
     */
    @Operation(
            summary = "Consulter son propre historique",
            description = "Récupère l'historique des actions de l'utilisateur actuellement connecté"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoriqueActionDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HistoriqueActionDTO>> getMonHistorique(Principal principal) {
        String email = principal.getName();
        Utilisateur utilisateur = utilisateurService.getByEmail(email);
        List<HistoriqueAction> historique = historiqueService.getHistoriqueParUtilisateur(utilisateur);
        List<HistoriqueActionDTO> historiqueDTO = historique.stream()
                .map(HistoriqueActionDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(historiqueDTO);
    }

    /**
     * Récupère l'historique des actions d'un utilisateur spécifique (réservé aux admins).
     *
     * @param id l'identifiant de l'utilisateur dont on veut l'historique
     * @return la liste des actions effectuées par cet utilisateur
     */
    @Operation(
            summary = "Consulter l'historique d'un utilisateur",
            description = "Récupère l'historique des actions d'un utilisateur spécifique (réservé aux administrateurs)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoriqueAction.class)))),
            @ApiResponse(responseCode = "403", description = "Accès interdit (non administrateur)"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @GetMapping("/utilisateur/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HistoriqueAction>> getHistoriqueParUtilisateur(@PathVariable Long id) {
        Utilisateur utilisateur = utilisateurService.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<HistoriqueAction> historique = historiqueService.getHistoriqueParUtilisateur(utilisateur);
        return ResponseEntity.ok(historique);
    }
}
