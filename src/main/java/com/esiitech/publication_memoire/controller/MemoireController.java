package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.MemoireDTO;
import com.esiitech.publication_memoire.dto.SoumissionMemoireRequest;
import com.esiitech.publication_memoire.dto.TransmissionLecteurRequest;
import com.esiitech.publication_memoire.entity.Memoire;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.StatutMemoire;
import com.esiitech.publication_memoire.mapper.MemoireMapper;
import com.esiitech.publication_memoire.repository.MemoireRepository;
import com.esiitech.publication_memoire.service.MemoireService;
import com.esiitech.publication_memoire.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/memoires")
@CrossOrigin(origins = "*")
@Slf4j
public class MemoireController {

    private final MemoireService memoireService;
    private final MemoireRepository memoireRepository;
    private final MemoireMapper memoireMapper;
    private final UtilisateurService utilisateurService;

    public MemoireController(MemoireService memoireService,
                             MemoireRepository memoireRepository,
                             MemoireMapper memoireMapper,
                             UtilisateurService utilisateurService) {
        this.memoireService = memoireService;
        this.memoireRepository = memoireRepository;
        this.memoireMapper = memoireMapper;
        this.utilisateurService = utilisateurService;
    }

    /**
     * Soumettre un mémoire par un étudiant.
     */
    @PostMapping("/etudiant/soumettre")
    @PreAuthorize("hasRole('ETUDIANT')") // S'assure que seul un étudiant peut soumettre
    public ResponseEntity<MemoireDTO> soumettreMemoire(@Valid @ModelAttribute SoumissionMemoireRequest request,
                                                       Principal principal) throws IOException {
        // 🔍 Récupération de l'utilisateur (étudiant) à partir de l'e-mail
        Utilisateur etudiant = utilisateurService.getByEmail(principal.getName());

        log.info("Soumission mémoire - Étudiant: {} ({})", etudiant.getNom(), etudiant.getEmail());

        // 🚀 Appel au service pour soumettre le mémoire
        MemoireDTO memoireDTO = memoireService.soumettreMemoire(
                etudiant.getId(),
                request.getTitre(),
                request.getDescription(),
                request.getFichierWord()
        );

        // Retour du DTO avec le statut HTTP 200 OK
        return ResponseEntity.ok(memoireDTO);
    }


    /**
     * Transmission d’un mémoire corrigé par un lecteur.
     */
    @PreAuthorize("hasRole('LECTEUR')")
    @PostMapping("/lecteur/transmettre")
    public ResponseEntity<MemoireDTO> transmettreParLecteur(
            @Valid @ModelAttribute TransmissionLecteurRequest request,
            Principal principal) throws IOException {

        Utilisateur lecteur = utilisateurService.getByEmail(principal.getName());

        log.info("Transmission mémoire ID: {} par lecteur ID: {}", request.getMemoireId(), lecteur.getId());

        MemoireDTO result = memoireService.transmettreParLecteur(
                request.getMemoireId(),
                lecteur.getId(),
                request.getCommentaire(),
                request.getFichierCorrige()
        );

        return ResponseEntity.ok(result);
    }


    /**
     * Télécharger le fichier original soumis par l’étudiant.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/telecharger")
    public ResponseEntity<Resource> telechargerFichierEtudiant(@PathVariable Long id) throws IOException {
        return memoireService.telechargerFichierOriginal(id);
    }

    /**
     * Afficher le mémoire en PDF directement dans le navigateur.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/preview/pdf")
    public ResponseEntity<Resource> previewPdf(@PathVariable Long id) throws IOException {
        Resource resource = memoireService.telechargerMemoirePdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Télécharger le PDF du mémoire corrigé ou validé.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/telecharger/pdf")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) throws IOException {
        Resource resource = memoireService.telechargerMemoirePdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Valider un mémoire (ADMIN).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/valider")
    public ResponseEntity<MemoireDTO> validerMemoire(@RequestParam Long memoireId, Principal principal) throws Exception {
        Utilisateur admin = utilisateurService.getByEmail(principal.getName());
        log.info("Validation mémoire ID: {} par admin ID: {}", memoireId, admin.getId());
        return ResponseEntity.ok(memoireService.validerMemoire(memoireId, admin.getId()));
    }

    /**
     * Rejeter un mémoire avec un commentaire (ADMIN).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/rejeter")
    public ResponseEntity<MemoireDTO> rejeterParAdmin(@RequestParam Long memoireId,
                                                      @RequestParam String commentaire) {
        log.info("Rejet mémoire ID: {}, commentaire: {}", memoireId, commentaire);
        MemoireDTO memoireDTO = memoireService.rejeterParAdmin(memoireId, commentaire);
        return ResponseEntity.ok(memoireDTO);
    }

    /**
     * Changer la visibilité d’un mémoire (public ou privé).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/visibilite")
    public ResponseEntity<MemoireDTO> changerVisibilite(@RequestParam Long memoireId,
                                                        @RequestParam boolean estPublic) {
        log.info("Changement visibilité mémoire ID: {}, estPublic: {}", memoireId, estPublic);
        MemoireDTO memoireDTO = memoireService.changerVisibilite(memoireId, estPublic);
        return ResponseEntity.ok(memoireDTO);
    }

    /**
     * Rechercher des mémoires par titre (accessibilité selon le rôle).
     */
    @GetMapping("/recherche")
    public ResponseEntity<List<MemoireDTO>> rechercheMemoire(@RequestParam(required = false) String titre,
                                                             Authentication authentication) {
        List<Memoire> memoires;

        boolean isAdmin = false;
        boolean isEtudiant = false;

        if (authentication != null) {
            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            isEtudiant = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ETUDIANT"));
        }

        if (isAdmin) {
            memoires = memoireRepository.findByTitre(titre);
        } else if (isEtudiant) {
            memoires = memoireRepository.findByStatutAndTitreContainingIgnoreCase(StatutMemoire.VALIDE, titre);
        } else {
            memoires = memoireRepository.findByStatutAndEstPublicAndTitreContainingIgnoreCase(
                    StatutMemoire.VALIDE, true, titre);
        }

        List<MemoireDTO> dtos = memoires.stream()
                .map(memoireMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**
     * Rechercher des mémoires par nom ou prénom de l'étudiant.
     */
    @GetMapping("/recherche-etudiant")
    public ResponseEntity<List<MemoireDTO>> chercherParNomPrenomEtudiant(@RequestParam String motCle,
                                                                         @AuthenticationPrincipal UserDetails userDetails) {
        List<Memoire> memoires = memoireService.chercherParNomOuPrenom(motCle, userDetails);
        List<MemoireDTO> dtos = memoires.stream()
                .map(memoireMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtenir les statistiques des mémoires selon le rôle.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(Principal principal) {
        Utilisateur utilisateur = utilisateurService.getByEmail(principal.getName());

        return switch (utilisateur.getRole()) {
            case ETUDIANT -> ResponseEntity.ok(memoireService.compterMemoiresEtudiant(utilisateur));
            case LECTEUR -> ResponseEntity.ok(memoireService.compterMemoiresLecteur(utilisateur));
            case ADMIN -> ResponseEntity.ok(memoireService.compterMemoiresAdmin());
            default -> ResponseEntity.status(403).build();
        };
    }

    /**
     * Obtenir un mémoire précis par son ID.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<MemoireDTO> getMemoireById(@PathVariable Long id) {
        MemoireDTO dto = memoireService.getMemoireById(id);
        return ResponseEntity.ok(dto);
    }


}
