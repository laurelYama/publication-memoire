package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.MemoireDTO;
import com.esiitech.publication_memoire.dto.UtilisateurDTO;
import com.esiitech.publication_memoire.entity.Memoire;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.Role;
import com.esiitech.publication_memoire.enums.StatutMemoire;
import com.esiitech.publication_memoire.mapper.MemoireMapper;
import com.esiitech.publication_memoire.repository.MemoireRepository;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import com.esiitech.publication_memoire.service.MemoireService;
import com.esiitech.publication_memoire.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UtilisateurService utilisateurService;
    private final UtilisateurRepository utilisateurRepository;
    private final MemoireService memoireService;
    private final MemoireRepository memoireRepository;
    private final MemoireMapper memoireMapper;

    public AdminController(UtilisateurService utilisateurService,
                           UtilisateurRepository utilisateurRepository,
                           MemoireService memoireService,
                           MemoireRepository memoireRepository,
                           MemoireMapper memoireMapper) {
        this.utilisateurService = utilisateurService;
        this.utilisateurRepository = utilisateurRepository;
        this.memoireService = memoireService;
        this.memoireRepository = memoireRepository;
        this.memoireMapper = memoireMapper;
    }

    @Operation(summary = "Ajouter un nouvel utilisateur", description = "Ajoute un utilisateur avec les détails fournis dans la requête.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou rôle incorrect")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/utilisateurs")
    public ResponseEntity<?> ajouterUtilisateur(@RequestBody UtilisateurDTO request) {
        if (request.getNom() == null || request.getPrenom() == null || request.getEmail() == null || request.getRole() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tous les champs sont requis, y compris le rôle."));
        }

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cet email est déjà utilisé."));
        }

        try {
            Role role = Role.valueOf(request.getRole().toUpperCase());
            Utilisateur utilisateur = utilisateurService.creerUtilisateur(
                    request.getNom(), request.getPrenom(), request.getEmail(), role
            );

            return ResponseEntity.ok(Map.of("message", "Utilisateur créé avec succès. Email d'activation envoyé."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le rôle fourni est invalide."));
        }
    }

    @Operation(summary = "Récupérer la liste des rôles disponibles", description = "Renvoie les rôles pouvant être assignés aux utilisateurs.")
    @ApiResponse(responseCode = "200", description = "Liste des rôles renvoyée avec succès")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles() {
        List<String> roles = Arrays.stream(Role.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Activer un utilisateur", description = "Active un utilisateur en fonction de son ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur activé avec succès"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/utilisateur/{id}/activer")
    public ResponseEntity<?> activerUtilisateur(@PathVariable Long id) {
        return utilisateurService.activerUtilisateur(id, true);
    }

    @Operation(summary = "Désactiver un utilisateur", description = "Désactive un utilisateur en fonction de son ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur désactivé avec succès"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/utilisateur/{id}/desactiver")
    public ResponseEntity<?> desactiverUtilisateur(@PathVariable Long id) {
        return utilisateurService.activerUtilisateur(id, false);
    }

    @Operation(summary = "Récupérer les lecteurs", description = "Renvoie la liste des lecteurs enregistrés dans le système.")
    @ApiResponse(responseCode = "200", description = "Liste des lecteurs renvoyée avec succès")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/lecteurs")
    public ResponseEntity<List<UtilisateurDTO>> getLecteurs() {
        List<UtilisateurDTO> lecteurs = utilisateurService.recupererLecteurs();
        return ResponseEntity.ok(lecteurs);
    }

    @Operation(summary = "Assigner un lecteur à un mémoire", description = "Assigne un lecteur à un mémoire spécifique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lecteur assigné avec succès au mémoire"),
            @ApiResponse(responseCode = "404", description = "Mémoire ou lecteur non trouvé")
    })
    @PutMapping("/assigner-lecteur")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemoireDTO> assignerLecteur(
            @RequestParam Long memoireId,
            @RequestParam Long lecteurId,
            Principal principal) {
        Utilisateur admin = utilisateurService.getByEmail(principal.getName());
        MemoireDTO memoireDTO = memoireService.assignerLecteur(memoireId, lecteurId, admin.getId());

        return ResponseEntity.ok(memoireDTO);
    }

    @Operation(summary = "Recherche flexible des mémoires", description = "Permet de rechercher des mémoires en utilisant divers critères.")
    @ApiResponse(responseCode = "200", description = "Liste des mémoires correspondant aux critères renvoyée avec succès")
    @GetMapping("/recherche/tous")
    public List<MemoireDTO> rechercherTousLesMemoires(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) StatutMemoire statut,
            @RequestParam(required = false) Boolean estPublic,
            @RequestParam(required = false) Long typeDocumentId,
            @RequestParam(required = false) String typeDocumentNom
    ) {
        List<Memoire> memoires = memoireRepository.rechercheFlexible(
                titre, nom, prenom, statut, estPublic, typeDocumentId, typeDocumentNom
        );
        return memoireMapper.toDtoList(memoires);
    }
}
