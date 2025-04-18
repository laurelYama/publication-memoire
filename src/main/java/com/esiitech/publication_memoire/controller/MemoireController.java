package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.MemoireDTO;
import com.esiitech.publication_memoire.dto.SoumissionMemoireRequest;
import com.esiitech.publication_memoire.dto.TransmissionLecteurRequest;
import com.esiitech.publication_memoire.entity.Memoire;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.StatutMemoire;
import com.esiitech.publication_memoire.exception.MemoireNotFoundException;
import com.esiitech.publication_memoire.mapper.MemoireMapper;
import com.esiitech.publication_memoire.repository.MemoireRepository;
import com.esiitech.publication_memoire.service.FileStorageService;
import com.esiitech.publication_memoire.service.MemoireService;
import com.esiitech.publication_memoire.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final FileStorageService fileStorageService;

    public MemoireController(MemoireService memoireService,
                             MemoireRepository memoireRepository,
                             MemoireMapper memoireMapper,
                             UtilisateurService utilisateurService,
                             FileStorageService fileStorageService) {
        this.memoireService = memoireService;
        this.memoireRepository = memoireRepository;
        this.memoireMapper = memoireMapper;
        this.utilisateurService = utilisateurService;
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "Soumettre un mémoire", description = "Permet à un étudiant de soumettre un mémoire")
    @ApiResponse(responseCode = "200", description = "Mémoire soumis avec succès", content = @Content(schema = @Schema(implementation = MemoireDTO.class)))
    @PostMapping("/etudiant/soumettre")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<MemoireDTO> soumettreMemoire(@Valid @ModelAttribute SoumissionMemoireRequest request,
                                                       Principal principal) throws IOException {
        Utilisateur etudiant = utilisateurService.getByEmail(principal.getName());
        MemoireDTO memoireDTO = memoireService.soumettreMemoire(
                etudiant.getId(),
                request.getTypeDocumentId(),
                request.getTitre(),
                request.getDescription(),
                request.getFichierWord()
        );
        return ResponseEntity.ok(memoireDTO);
    }

    @Operation(summary = "Transmettre un mémoire corrigé", description = "Transmission par un lecteur après correction")
    @PostMapping("/lecteur/transmettre")
    @PreAuthorize("hasRole('LECTEUR')")
    public ResponseEntity<MemoireDTO> transmettreParLecteur(
            @Valid @ModelAttribute TransmissionLecteurRequest request,
            Principal principal) throws IOException {
        Utilisateur lecteur = utilisateurService.getByEmail(principal.getName());
        MemoireDTO result = memoireService.transmettreParLecteur(
                request.getMemoireId(),
                lecteur.getId(),
                request.getCommentaire(),
                request.getFichierCorrige()
        );
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Télécharger le fichier d’un mémoire")
    @GetMapping("/{id}/telecharger")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> telechargerFichierEtudiant(@PathVariable Long id) throws IOException {
        return memoireService.telechargerFichierOriginal(id);
    }

    @Operation(summary = "Afficher ou télécharger le PDF validé d’un mémoire")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> telechargerOuAfficherMemoirePdf(
            @PathVariable Long id,
            @RequestParam(defaultValue = "download") String mode) {
        Memoire memoire = memoireRepository.findById(id)
                .orElseThrow(() -> new MemoireNotFoundException(id));
        if (memoire.getStatut() != StatutMemoire.VALIDE || memoire.getFichierPdf() == null) {
            throw new IllegalStateException("Le mémoire n’est pas encore publié.");
        }
        Resource resource = fileStorageService.chargerFichier(memoire.getFichierPdf());
        String nomFichier = memoire.getTitre().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
        String disposition = mode.equalsIgnoreCase("inline") ? "inline" : "attachment";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + nomFichier + "\"")
                .body(resource);
    }

    @Operation(summary = "Valider un mémoire", description = "Valider un mémoire par l’admin")
    @PostMapping("/admin/valider")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemoireDTO> validerMemoire(@RequestParam Long memoireId, Principal principal) throws Exception {
        Utilisateur admin = utilisateurService.getByEmail(principal.getName());
        return ResponseEntity.ok(memoireService.validerMemoire(memoireId, admin.getId()));
    }

    @Operation(summary = "Rejeter un mémoire", description = "Rejet par l’admin avec un commentaire")
    @PostMapping("/admin/rejeter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemoireDTO> rejeterParAdmin(@RequestParam Long memoireId,
                                                      @RequestParam String commentaire) {
        MemoireDTO memoireDTO = memoireService.rejeterParAdmin(memoireId, commentaire);
        return ResponseEntity.ok(memoireDTO);
    }

    @Operation(summary = "Changer la visibilité d’un mémoire")
    @PutMapping("/visibilite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemoireDTO> changerVisibilite(@RequestParam Long memoireId,
                                                        @RequestParam boolean estPublic) {
        MemoireDTO memoireDTO = memoireService.changerVisibilite(memoireId, estPublic);
        return ResponseEntity.ok(memoireDTO);
    }

    @Operation(summary = "Obtenir les statistiques des mémoires")
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getStats(Principal principal) {
        Utilisateur utilisateur = utilisateurService.getByEmail(principal.getName());
        return switch (utilisateur.getRole()) {
            case ETUDIANT -> ResponseEntity.ok(memoireService.compterMemoiresEtudiant(utilisateur));
            case LECTEUR -> ResponseEntity.ok(memoireService.compterMemoiresLecteur(utilisateur));
            case ADMIN -> ResponseEntity.ok(memoireService.compterMemoiresAdmin());
            default -> ResponseEntity.status(403).build();
        };
    }

    @Operation(summary = "Obtenir un mémoire par ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MemoireDTO> getMemoireById(@PathVariable Long id) {
        MemoireDTO dto = memoireService.getMemoireById(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Rechercher des mémoires publiés", description = "Recherche publique")
    @GetMapping("/public/recherche")
    public List<MemoireDTO> rechercherMemoiresPublics(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) Long typeDocumentId,
            @RequestParam(required = false) String typeDocumentNom
    ) {
        List<Memoire> memoires = memoireService.chercherMemoiresPublics(titre, nom, prenom, typeDocumentId, typeDocumentNom);
        return memoireMapper.toDtoList(memoires);
    }

    @Operation(summary = "Rechercher des mémoires (utilisateur connecté)")
    @GetMapping("/recherche")
    public List<MemoireDTO> rechercherMemoires(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) Long typeDocumentId,
            @RequestParam(required = false) String typeDocumentNom
    ) {
        List<Memoire> memoires = memoireRepository.rechercherMemoiresValides(titre, nom, prenom, typeDocumentId, typeDocumentNom);
        return memoireMapper.toDtoList(memoires);
    }

    @Operation(summary = "Re-soumettre un mémoire après correction")
    @PutMapping("/etudiant/{id}/resoumettre")
    @PreAuthorize("hasRole('ETUDIANT')")
    public MemoireDTO resoumettreMemoire(@PathVariable Long id, @RequestParam("fichier") MultipartFile fichier) throws IOException {
        Utilisateur etudiant = utilisateurService.getUtilisateurConnecte();
        return memoireService.reSoumettreMemoire(id, fichier, etudiant);
    }
}
