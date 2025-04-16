package com.esiitech.publication_memoire.service;

import com.esiitech.publication_memoire.dto.MemoireDTO;
import com.esiitech.publication_memoire.entity.Memoire;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.Role;
import com.esiitech.publication_memoire.enums.StatutMemoire;
import com.esiitech.publication_memoire.exception.MemoireNotFoundException;
import com.esiitech.publication_memoire.exception.UtilisateurNotFoundException;
import com.esiitech.publication_memoire.logging.Loggable;
import com.esiitech.publication_memoire.mapper.MemoireMapper;
import com.esiitech.publication_memoire.repository.MemoireRepository;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.esiitech.publication_memoire.enums.StatutMemoire.VALIDE;

@Service
public class MemoireService {
    private static final Logger logger = LoggerFactory.getLogger(MemoireService.class);

    private final MemoireRepository memoireRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailService emailService;
    private final MemoireMapper memoireMapper;
    private final HistoriqueActionService historiqueActionService;
    private final FileStorageService fileStorageService;
    private final FileConversionService fileConversionService;

    // Types MIME acceptés pour les documents Word
    private static final String[] WORD_MIME_TYPES = {
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/msword" // .doc
    };

    @Value("${app.storage.base-path}")
    private String basePath;

    public MemoireService(
            MemoireRepository memoireRepository,
            UtilisateurRepository utilisateurRepository,
            EmailService emailService,
            MemoireMapper memoireMapper,
            HistoriqueActionService historiqueActionService,
            FileStorageService fileStorageService,
            FileConversionService fileConversionService) {
        this.memoireRepository = memoireRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.emailService = emailService;
        this.memoireMapper = memoireMapper;
        this.historiqueActionService = historiqueActionService;
        this.fileStorageService = fileStorageService;
        this.fileConversionService = fileConversionService;
    }

    /**
     * Permet à un étudiant de soumettre un mémoire.
     */
    @Loggable
    @Transactional
    public MemoireDTO soumettreMemoire(Long etudiantId, String titre, String description, MultipartFile fichierWord) throws IOException {
        // 1. Vérifie que l'étudiant existe
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new UtilisateurNotFoundException(etudiantId));

        // 2. Sauvegarde du fichier mémoire
        String cheminFichier = fileStorageService.sauvegarderFichier(fichierWord, "mémoires", WORD_MIME_TYPES);

        // 3. Création du mémoire
        Memoire memoire = new Memoire();
        memoire.setTitre(titre);
        memoire.setDescription(description);
        memoire.setFichierWord(cheminFichier);
        memoire.setEtudiant(etudiant);
        memoire.setStatut(StatutMemoire.EN_ATTENTE);

        // 4. Sauvegarde du mémoire
        Memoire saved = memoireRepository.save(memoire);

        // 5. Historique pour l'étudiant
        historiqueActionService.enregistrerAction(etudiant, "Soumission du mémoire", saved);

        // 6. Récupération du premier admin
        Utilisateur admin = utilisateurRepository.findFirstByRoleOrderByIdAsc(Role.ADMIN)
                .orElseThrow(() -> new UtilisateurNotFoundException("Aucun administrateur trouvé"));

        // 7. Notification par mail à l'admin (envoi asynchrone)
        this.envoyerNotificationAdmin(saved, etudiant, admin);

        // 8. Notification par mail à l'étudiant (envoi asynchrone)
        this.envoyerConfirmationEtudiant(saved, etudiant);

        // 9. Historique côté admin (notification reçue)
        historiqueActionService.enregistrerAction(admin, "Notification reçue : Soumission mémoire de " + etudiant.getNom(), saved);

        logger.info("Mémoire soumis avec succès : ID={}, Étudiant={}", saved.getId(), etudiant.getNom());
        return memoireMapper.toDto(saved);
    }

    private void envoyerNotificationAdmin(Memoire memoire, Utilisateur etudiant, Utilisateur admin) {
        String sujetAdmin = "Nouveau mémoire soumis";
        String contenuAdmin = """
            Bonjour %s,

            Un nouveau mémoire a été soumis par l'étudiant %s (%s).

            Titre du mémoire : %s
            Description : %s

            Veuillez vous connecter à l'administration pour le consulter.

            Cordialement,
            Système de gestion des mémoires
            """.formatted(admin.getNom(), etudiant.getNom(), etudiant.getEmail(), memoire.getTitre(), memoire.getDescription());

        // Envoi asynchrone pour ne pas bloquer le thread principal
        emailService.sendEmailAsync(admin.getEmail(), sujetAdmin, contenuAdmin);
    }

    private void envoyerConfirmationEtudiant(Memoire memoire, Utilisateur etudiant) {
        String sujetEtudiant = "Votre mémoire a bien été soumis";
        String contenuEtudiant = """
            Bonjour %s,

            Votre mémoire intitulé « %s » a été soumis avec succès. Il est actuellement en attente de validation par l'administration.

            Vous recevrez une notification dès qu'un lecteur vous sera assigné ou qu'une action sera effectuée sur votre mémoire.

            Merci pour votre soumission et bonne chance pour la suite !

            Cordialement,
            Équipe de gestion des mémoires
            """.formatted(etudiant.getNom(), memoire.getTitre());

        // Envoi asynchrone
        emailService.sendEmailAsync(etudiant.getEmail(), sujetEtudiant, contenuEtudiant);
    }

    /**
     * Permet au lecteur de transmettre un mémoire corrigé.
     */
    @Loggable
    @Transactional
    public MemoireDTO transmettreParLecteur(Long memoireId, Long lecteurId, String commentaire, MultipartFile fichierCorrige) throws IOException {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new MemoireNotFoundException(memoireId));

        Utilisateur lecteur = utilisateurRepository.findById(lecteurId)
                .orElseThrow(() -> new UtilisateurNotFoundException(lecteurId));

        if (fichierCorrige == null || fichierCorrige.isEmpty()) {
            throw new IllegalArgumentException("Le fichier corrigé est requis.");
        }

        String cheminCorrige = fileStorageService.sauvegarderFichier(fichierCorrige, "corrections", WORD_MIME_TYPES);
        memoire.setFichierCorrige(cheminCorrige);
        memoire.setLecteur(lecteur);
        memoire.setStatut(StatutMemoire.TRANSMIS);
        memoire.setCommentaire(commentaire);
        memoire.setTransmisAAdmin(true);


        Utilisateur admin = utilisateurRepository.findFirstByRoleOrderByIdAsc(Role.ADMIN)
                .orElseThrow(() -> new UtilisateurNotFoundException("Aucun administrateur trouvé"));

        // Notification asynchrone à l'admin
        emailService.sendEmailAsync(admin.getEmail(), "Mémoire transmis par le lecteur",
                String.format("""
                    Bonjour %s,
                    
                    Le mémoire "%s" a été corrigé et transmis par %s %s.
                    
                    Commentaire : %s
                    
                    Veuillez vous connecter à l'administration pour le valider.
                    
                    Cordialement,
                    Système de gestion des mémoires
                    """,
                        admin.getNom(), memoire.getTitre(), lecteur.getPrenom(), lecteur.getNom(),
                        commentaire != null ? commentaire : "Aucun commentaire"));

        Memoire updated = memoireRepository.save(memoire);
        historiqueActionService.enregistrerAction(lecteur, "Transmission du mémoire corrigé", updated);

        logger.info("Mémoire transmis par le lecteur : ID={}, Lecteur={}", memoireId, lecteur.getNom());
        return memoireMapper.toDto(updated);
    }

    /**
     * Permet à l'administrateur de valider un mémoire.
     */
    @Transactional
    @Loggable
    public MemoireDTO validerMemoire(Long memoireId, Long adminId) throws Exception {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new EntityNotFoundException("Mémoire introuvable avec ID : " + memoireId));

        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Administrateur introuvable avec ID : " + adminId));

        if (memoire.getFichierCorrige() == null) {
            throw new IllegalStateException("Aucun fichier corrigé à convertir.");
        }

        String cheminPdf = fileConversionService.convertirWordEnPdf(memoire.getFichierCorrige());
        memoire.setFichierPdf(cheminPdf);
        memoire.setStatut(VALIDE);

        Utilisateur etudiant = memoire.getEtudiant();
        emailService.sendEmailAsync(etudiant.getEmail(), "Mémoire validé",
                "Félicitations, votre mémoire a été validé et est publié en PDF.");

        Memoire updated = memoireRepository.save(memoire);
        historiqueActionService.enregistrerAction(admin, "Validation du mémoire", updated);

        return memoireMapper.toDto(updated);
    }


    /**
     * Télécharge le fichier original d'un mémoire
     */
    @Loggable
    public ResponseEntity<Resource> telechargerFichierOriginal(Long memoireId) {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new MemoireNotFoundException(memoireId));

        Resource resource = fileStorageService.chargerFichier(memoire.getFichierWord());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                        memoire.getTitre().replaceAll("[^a-zA-Z0-9]", "_") +
                        memoire.getFichierWord().substring(memoire.getFichierWord().lastIndexOf(".")) + "\"")
                .body(resource);
    }

    /**
     * Permet à l’administrateur de rejeter un mémoire avec un commentaire.
     * L’étudiant est notifié par email.
     */
    @Loggable
    public MemoireDTO rejeterParAdmin(Long memoireId, String commentaire) {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new EntityNotFoundException("Mémoire introuvable avec l'ID : " + memoireId));

        memoire.setStatut(StatutMemoire.REJETE);
        memoire.setCommentaire(commentaire);

        Utilisateur etudiant = memoire.getEtudiant();
        if (etudiant != null && etudiant.getEmail() != null) {
            emailService.sendEmailAsync(
                    etudiant.getEmail(),
                    "Rejet de votre mémoire",
                    String.format("Bonjour %s,\n\nVotre mémoire \"%s\" a été rejeté pour la raison suivante :\n%s\n\nMerci de corriger et soumettre à nouveau.\n\nCordialement,\nL'équipe.",
                            etudiant.getPrenom(), memoire.getTitre(), commentaire)
            );
        }

        Memoire updated = memoireRepository.save(memoire);
        historiqueActionService.enregistrerAction(etudiant, "Mémoire rejeté par l’administrateur", updated);

        return memoireMapper.toDto(updated);
    }

    /**
     * Permet de changer la visibilité d’un mémoire (public/privé).
     */
    @Loggable
    public MemoireDTO changerVisibilite(Long memoireId, boolean estPublic) {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new EntityNotFoundException("Mémoire introuvable avec l'ID : " + memoireId));

        memoire.setEstPublic(estPublic);

        Memoire updated = memoireRepository.save(memoire);
        historiqueActionService.enregistrerAction(
                memoire.getEtudiant(), estPublic ? "Mémoire rendu public" : "Mémoire rendu privé", updated
        );

        return memoireMapper.toDto(updated);
    }

    /**
     * Compte les mémoires d'un étudiant par statut (valide, rejeté, transmis).
     *
     * @param etudiant L'utilisateur de type étudiant.
     * @return Une map contenant les statistiques de ses mémoires.
     */
    public Map<String, Long> compterMemoiresEtudiant(Utilisateur etudiant) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("valide", memoireRepository.countByEtudiantAndStatut(etudiant, StatutMemoire.VALIDE));
        stats.put("rejete", memoireRepository.countByEtudiantAndStatut(etudiant, StatutMemoire.REJETE));
        stats.put("transmis", memoireRepository.countByEtudiantAndStatut(etudiant, StatutMemoire.TRANSMIS));
        return stats;
    }


    /**
     * Compte les mémoires associés à un lecteur par type de traitement.
     *
     * @param lecteur L'utilisateur de type lecteur.
     * @return Une map avec les statistiques : reçus, envoyés à l'admin, non traités.
     */
    public Map<String, Long> compterMemoiresLecteur(Utilisateur lecteur) {
        Map<String, Long> stats = new HashMap<>();

        // TOUS les mémoires affectés à ce lecteur, peu importe le statut ou la transmission
        long totalRecus = memoireRepository.countByLecteur(lecteur);

        // Ceux transmis à l'admin
        long envoyesAdmin = memoireRepository.countByLecteurAndTransmisAAdminTrue(lecteur);

        // Ceux encore en attente de traitement
        long nonTraites = totalRecus - envoyesAdmin;

        stats.put("recus", totalRecus);
        stats.put("envoyesAdmin", envoyesAdmin);
        stats.put("nonTraites", nonTraites);

        return stats;
    }




    /**
     * Compte tous les mémoires par statut pour l'administrateur.
     *
     * @return Une map avec le nombre total de mémoires par statut.
     */
    public Map<String, Long> compterMemoiresAdmin() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("valide", memoireRepository.countByStatut(StatutMemoire.VALIDE));
        stats.put("rejete", memoireRepository.countByStatut(StatutMemoire.REJETE));
        stats.put("transmis", memoireRepository.countByStatut(StatutMemoire.TRANSMIS));
        return stats;
    }

    public MemoireDTO assignerLecteur(Long memoireId, Long lecteurId, Long adminId) {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new EntityNotFoundException("Mémoire non trouvé avec l'ID : " + memoireId));

        Utilisateur lecteur = utilisateurRepository.findById(lecteurId)
                .orElseThrow(() -> new EntityNotFoundException("Lecteur non trouvé avec l'ID : " + lecteurId));

        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Administrateur introuvable avec l'ID : " + adminId));

        memoire.setLecteur(lecteur);
        memoire.setStatut(StatutMemoire.EN_ATTENTE); // ou autre statut pertinent

        Memoire updatedMemoire = memoireRepository.save(memoire);

        // Historique
        historiqueActionService.enregistrerAction(admin, "Mémoire assigné au lecteur " + lecteur.getNom(), memoire);

        // Notifications par mail
        String sujetLecteur = "Nouveau mémoire assigné";
        String contenuLecteur = "Vous avez été assigné comme lecteur du mémoire : " + memoire.getTitre();


        emailService.sendEmailAsync(lecteur.getEmail(), sujetLecteur, contenuLecteur);

        return memoireMapper.toDto(updatedMemoire);
    }

    public MemoireDTO getMemoireById(Long id) {
        Memoire memoire = memoireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mémoire non trouvé avec l'id: " + id));
        return memoireMapper.toDto(memoire);
    }




    public List<Memoire> chercherMemoiresPublics(String titre, String nom, String prenom) {
        return memoireRepository.rechercherMemoiresPublicsValides(titre, nom, prenom);
    }

}
