package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.HistoriqueActionDTO;
import com.esiitech.publication_memoire.entity.HistoriqueAction;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.service.HistoriqueActionService;
import com.esiitech.publication_memoire.service.UtilisateurService;
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
public class HistoriqueActionController {

    private final HistoriqueActionService historiqueService;
    private final UtilisateurService utilisateurService;

    /**
     * Constructeur avec injection des services nécessaires.
     *
     * @param historiqueService le service de gestion des historiques
     * @param utilisateurService le service de gestion des utilisateurs
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
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HistoriqueActionDTO>> getMonHistorique(Principal principal) {
        // Récupère l'e-mail de l'utilisateur connecté
        String email = principal.getName();

        // Récupère l'objet utilisateur complet depuis le service
        Utilisateur utilisateur = utilisateurService.getByEmail(email);

        // Récupère les actions de cet utilisateur
        List<HistoriqueAction> historique = historiqueService.getHistoriqueParUtilisateur(utilisateur);

        // Convertit en DTO pour la réponse
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
    @GetMapping("/utilisateur/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HistoriqueAction>> getHistoriqueParUtilisateur(@PathVariable Long id) {
        // Vérifie si l'utilisateur existe
        Utilisateur utilisateur = utilisateurService.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Récupère et retourne l'historique
        List<HistoriqueAction> historique = historiqueService.getHistoriqueParUtilisateur(utilisateur);
        return ResponseEntity.ok(historique);
    }
}
