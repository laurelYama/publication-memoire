package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.MemoireDTO;
import com.esiitech.publication_memoire.entity.Memoire;
import com.esiitech.publication_memoire.mapper.MemoireMapper;
import com.esiitech.publication_memoire.service.MemoireService;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/memoires")
@CrossOrigin(origins = "*") // À adapter si besoin
public class MemoireController {

    private final MemoireService memoireService;
    private final MemoireMapper memoireMapper;

    @Autowired
    public MemoireController(MemoireService memoireService, MemoireMapper memoireMapper) {
        this.memoireService = memoireService;
        this.memoireMapper = memoireMapper;
    }

    // Soumettre un mémoire par un étudiant
    @PostMapping("/soumettre")
    public ResponseEntity<MemoireDTO> soumettreMemoire(
            @RequestParam Long etudiantId,
            @RequestParam String titre,
            @RequestParam String description,
            @RequestParam MultipartFile fichierWord
    ) throws IOException {
        MemoireDTO memoireDTO = memoireService.soumettreMemoire(etudiantId, titre, description, fichierWord);
        return ResponseEntity.ok(memoireDTO);
    }


    // Le lecteur transmet le mémoire à l’admin
    @PostMapping("/transmettre")
    public ResponseEntity<MemoireDTO> transmettreParLecteur(
            @RequestParam Long memoireId,
            @RequestParam Long lecteurId,
            @RequestParam String commentaire
    ) {
        MemoireDTO memoireDto = memoireService.transmettreParLecteur(memoireId, lecteurId, commentaire);
        return ResponseEntity.ok(memoireDto);
    }


    // L’admin valide le mémoire
    @PostMapping("/valider/{memoireId}")
    public ResponseEntity<Memoire> validerParAdmin(@PathVariable Long memoireId) {
        Memoire memoire = memoireService.validerParAdmin(memoireId);
        return ResponseEntity.ok(memoire);
    }

    // L’admin rejette le mémoire
    @PostMapping("/rejeter")
    public ResponseEntity<MemoireDTO> rejeterParAdmin(
            @RequestParam Long memoireId,
            @RequestParam String commentaire
    ) {
        MemoireDTO memoireDTO = memoireService.rejeterParAdmin(memoireId, commentaire);
        return ResponseEntity.ok(memoireDTO);
    }


    // Changer la visibilité du mémoire (public/privé)
    @PutMapping("/visibilite")
    public ResponseEntity<MemoireDTO> changerVisibilite(
            @RequestParam Long memoireId,
            @RequestParam boolean estPublic
    ) {
        MemoireDTO memoireDTO = memoireService.changerVisibilite(memoireId, estPublic);
        return ResponseEntity.ok(memoireDTO);
    }


    // Récupérer la liste des mémoires publics validés
    @GetMapping("/publics")
    public ResponseEntity<List<Memoire>> getMemoiresPublics() {
        List<Memoire> memoires = memoireService.getMemoiresPublics();
        return ResponseEntity.ok(memoires);
    }


    @GetMapping("/memoires/{id}")
    public ResponseEntity<MemoireDTO> getMemoireById(@PathVariable Long id) {
        MemoireDTO dto = memoireService.getMemoireById(id);
        return ResponseEntity.ok(dto);
    }


    @GetMapping("/telecharger/{memoireId}")
    public ResponseEntity<?> telechargerFichier(@PathVariable Long memoireId) {
        try {
            Resource fichier = memoireService.getFichierMemoire(memoireId);

            if (fichier == null || !fichier.exists()) {
                return ResponseEntity
                        .status(404)
                        .body("Fichier non trouvé pour le mémoire ID : " + memoireId);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fichier.getFilename() + "\"")
                    .body(fichier);

        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Erreur lors du téléchargement du fichier : " + e.getMessage());
        }
    }




}
