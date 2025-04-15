package com.esiitech.publication_memoire.service;

import com.esiitech.publication_memoire.config.StorageProperties;
import com.esiitech.publication_memoire.dto.MemoireDTO;
import com.esiitech.publication_memoire.entity.Memoire;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.Role;
import com.esiitech.publication_memoire.enums.StatutMemoire;
import com.esiitech.publication_memoire.logging.Loggable;
import com.esiitech.publication_memoire.mapper.MemoireMapper;
import com.esiitech.publication_memoire.repository.MemoireRepository;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.esiitech.publication_memoire.enums.StatutMemoire.VALIDE;

@Service
public class MemoireService {

    @Value("${app.storage.base-path}")
    private String basePath;

    private final MemoireRepository memoireRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailService emailService;
    private final MemoireMapper memoireMapper;
    private final StorageProperties storageProperties;
    private final HistoriqueActionService historiqueActionService;

    // Constructeur injectant les dépendances nécessaires
    public MemoireService(
            MemoireRepository memoireRepository,
            UtilisateurRepository utilisateurRepository,
            EmailService emailService,
            MemoireMapper memoireMapper,
            StorageProperties storageProperties,
            HistoriqueActionService historiqueActionService) {
        this.memoireRepository = memoireRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.emailService = emailService;
        this.memoireMapper = memoireMapper;
        this.storageProperties = storageProperties;
        this.historiqueActionService = historiqueActionService;
    }

    /**
     * Permet à un étudiant de soumettre un mémoire.
     * Le fichier est enregistré et le mémoire est sauvegardé avec le statut EN_ATTENTE.
     */
    @Loggable
    public MemoireDTO soumettreMemoire(Long etudiantId, String titre, String description, MultipartFile fichierWord) throws IOException {
        // 1. Vérifie que l'étudiant existe
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new EntityNotFoundException("Étudiant introuvable avec l'ID : " + etudiantId));

        // 2. Sauvegarde du fichier mémoire
        String cheminFichier = sauvegarderFichier(fichierWord, "mémoires");

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
                .orElseThrow(() -> new EntityNotFoundException("Aucun administrateur trouvé"));

        // 7. Notification par mail à l’admin
        String sujetAdmin = "📘 Nouveau mémoire soumis";
        String contenuAdmin = """
            Bonjour %s,

            Un nouveau mémoire a été soumis par l'étudiant %s (%s).

            Titre du mémoire : %s
            Description : %s

            Veuillez vous connecter à l'administration pour le consulter.

            Cordialement,
            Système de gestion des mémoires
            """.formatted(admin.getNom(), etudiant.getNom(), etudiant.getEmail(), titre, description);

        emailService.sendEmail(admin.getEmail(), sujetAdmin, contenuAdmin);

        // 8. Notification par mail à l’étudiant
        String sujetEtudiant = "✅ Votre mémoire a bien été soumis";
        String contenuEtudiant = """
            Bonjour %s,

            Votre mémoire intitulé « %s » a été soumis avec succès. Il est actuellement en attente de validation par l’administration.

            Vous recevrez une notification dès qu’un lecteur vous sera assigné ou qu’une action sera effectuée sur votre mémoire.

            Merci pour votre soumission et bonne chance pour la suite !

            Cordialement,
            Équipe de gestion des mémoires
            """.formatted(etudiant.getNom(), titre);

        emailService.sendEmail(etudiant.getEmail(), sujetEtudiant, contenuEtudiant);

        // 9. Historique côté admin (notification reçue)
        historiqueActionService.enregistrerAction(admin, "Notification reçue : Soumission mémoire de " + etudiant.getNom(), saved);

        return memoireMapper.toDto(saved);
    }



    /**
     * Permet au lecteur de transmettre un mémoire corrigé.
     * Le fichier corrigé est enregistré et un mail est envoyé à l'administrateur.
     */
    @Loggable
    public MemoireDTO transmettreParLecteur(Long memoireId, Long lecteurId, String commentaire, MultipartFile fichierCorrige) throws IOException {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new EntityNotFoundException("Mémoire introuvable avec ID : " + memoireId));

        Utilisateur lecteur = utilisateurRepository.findById(lecteurId)
                .orElseThrow(() -> new EntityNotFoundException("Lecteur introuvable avec ID : " + lecteurId));

        if (fichierCorrige == null || fichierCorrige.isEmpty()) {
            throw new IllegalArgumentException("Le fichier corrigé est requis.");
        }

        String cheminCorrige = sauvegarderFichier(fichierCorrige, "corrections");
        memoire.setFichierCorrige(cheminCorrige);
        memoire.setLecteur(lecteur);
        memoire.setStatut(StatutMemoire.TRANSMIS);
        memoire.setCommentaire(commentaire);

        Utilisateur admin = utilisateurRepository.findFirstByRoleOrderByIdAsc(Role.ADMIN)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Administrateur introuvable."));

        emailService.sendEmail(admin.getEmail(), "Mémoire transmis par le lecteur",
                String.format("Bonjour,\n\nLe mémoire \"%s\" a été corrigé et transmis par %s %s.\n\nCommentaire : %s",
                        memoire.getTitre(), lecteur.getPrenom(), lecteur.getNom(), commentaire != null ? commentaire : "Aucun commentaire"));

        Memoire updated = memoireRepository.save(memoire);
        historiqueActionService.enregistrerAction(lecteur, "Transmission du mémoire corrigé", updated);

        return memoireMapper.toDto(updated);
    }

    /**
     * Permet à l’administrateur de valider un mémoire.
     * Le fichier corrigé est converti en PDF, le statut passe à VALIDE.
     */
    @Loggable
    public MemoireDTO validerMemoire(Long memoireId, Long adminId) throws Exception {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new EntityNotFoundException("Mémoire introuvable avec ID : " + memoireId));

        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Administrateur introuvable avec ID : " + adminId));

        if (memoire.getFichierCorrige() == null) {
            throw new IllegalStateException("Aucun fichier corrigé à convertir.");
        }

        String cheminPdf = convertirWordEnPdf(memoire.getFichierCorrige());
        memoire.setFichierPdf(cheminPdf);
        memoire.setStatut(VALIDE);

        Utilisateur etudiant = memoire.getEtudiant();
        emailService.sendEmail(etudiant.getEmail(), "Mémoire validé",
                "Félicitations, votre mémoire a été validé et est publié en PDF.");

        Memoire updated = memoireRepository.save(memoire);
        historiqueActionService.enregistrerAction(admin, "Validation du mémoire", updated);

        return memoireMapper.toDto(updated);
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
            emailService.sendEmail(
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
     * Enregistre un fichier envoyé (Word uniquement) dans un dossier cible.
     * Vérifie le format et génère un nom unique.
     */
    private String sauvegarderFichier(MultipartFile fichier, String dossierCible) throws IOException {
        if (fichier.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide.");
        }

        String originalFilename = fichier.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".doc") && !originalFilename.toLowerCase().endsWith(".docx"))) {
            throw new IllegalArgumentException("Le fichier doit être au format Word (.doc ou .docx)");
        }

        String nomUnique = UUID.randomUUID() + "_" + originalFilename;

        Path dossierPath = Paths.get(storageProperties.getUploadDir(), dossierCible).toAbsolutePath().normalize();
        Files.createDirectories(dossierPath);

        Path cheminFichier = dossierPath.resolve(nomUnique);
        fichier.transferTo(cheminFichier.toFile());

        return dossierCible + "/" + nomUnique;
    }

    /**
     * Convertit un fichier Word (.doc/.docx) en PDF via LibreOffice en ligne de commande.
     * Le fichier PDF est enregistré dans le dossier défini dans la configuration.
     */
    private String convertirWordEnPdf(String cheminRelatifFichierWord) throws IOException, InterruptedException {
        Path cheminAbsolu = Paths.get(storageProperties.getUploadDir(), cheminRelatifFichierWord)
                .toAbsolutePath().normalize();
        File fichierWord = cheminAbsolu.toFile();

        if (!fichierWord.exists()) {
            throw new FileNotFoundException("Fichier Word introuvable : " + fichierWord.getAbsolutePath());
        }

        String nomFichierPdf = fichierWord.getName().replaceAll("\\.(doc|docx)$", ".pdf");

        File dossierPdf = new File(storageProperties.getPdfDir());
        if (!dossierPdf.exists()) dossierPdf.mkdirs();

        String cheminFichierPdf = Paths.get(storageProperties.getPdfDir(), nomFichierPdf).toString();

        // Utilisation de LibreOffice pour convertir le fichier
        ProcessBuilder pb = new ProcessBuilder(
                storageProperties.getSofficePath(),
                "--headless",
                "--convert-to", "pdf",
                "--outdir", dossierPdf.getAbsolutePath(),
                fichierWord.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                System.out.println("[LibreOffice] " + ligne);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Erreur lors de la conversion en PDF (code " + exitCode + ").");
        }

        return cheminFichierPdf;
    }

    /**
     * Télécharge le fichier PDF d'un mémoire à partir de son ID.
     *
     * @param id L'identifiant du mémoire.
     * @return Le fichier PDF en tant que ressource téléchargeable.
     * @throws IOException Si le fichier est introuvable ou illisible.
     */
    public Resource telechargerMemoirePdf(Long id) throws IOException {
        Memoire memoire = memoireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mémoire introuvable avec l'ID: " + id));

        String cheminFichier = storageProperties.getBasePath() + "/" + memoire.getFichierPdf();
        Path path = Paths.get(cheminFichier);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Fichier non lisible ou introuvable: " + cheminFichier);
        }

        return resource;
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

        long recus = memoireRepository.countByLecteurAndStatutAndTransmisAAdminFalse(lecteur, StatutMemoire.EN_ATTENTE);
        long envoyesAdmin = memoireRepository.countByLecteurAndStatutAndTransmisAAdminTrue(lecteur, StatutMemoire.EN_ATTENTE);

        stats.put("recus", recus);
        stats.put("envoyesAdmin", envoyesAdmin);
        stats.put("nonTraites", recus); // Les non traités sont ceux reçus mais pas encore transmis à l’admin

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

        String sujetEtudiant = "Votre mémoire a été assigné à un lecteur";
        String contenuEtudiant = "Votre mémoire '" + memoire.getTitre() + "' a été assigné à " + lecteur.getNom();

        emailService.sendEmail(lecteur.getEmail(), sujetLecteur, contenuLecteur);
        emailService.sendEmail(memoire.getEtudiant().getEmail(), sujetEtudiant, contenuEtudiant);

        return memoireMapper.toDto(updatedMemoire);
    }



    /**
     * Recherche des mémoires par nom ou prénom de l'étudiant en fonction du rôle de l'utilisateur connecté.
     *
     * @param motCle      Le mot-clé à rechercher (nom ou prénom).
     * @param userDetails L'utilisateur connecté.
     * @return La liste des mémoires correspondant aux critères de recherche.
     */
    public List<Memoire> chercherParNomOuPrenom(String motCle, UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isEtudiant = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ETUDIANT"));

        if (isAdmin) {
            return memoireRepository.searchByNomOrPrenomEtudiant(motCle);
        } else if (isEtudiant) {
            return memoireRepository.searchByNomOrPrenomEtudiantAndStatut(motCle, StatutMemoire.VALIDE);
        } else {
            return memoireRepository.searchByNomOrPrenomEtudiantAndStatutAndEstPublic(motCle, StatutMemoire.VALIDE, true);
        }
    }


    /**
     * Télécharge le fichier original (ex: Word) d’un mémoire.
     *
     * @param memoireId L’identifiant du mémoire.
     * @return La réponse contenant le fichier original en tant que ressource.
     * @throws IOException Si le fichier n’est pas trouvé.
     */
    @Loggable
    public ResponseEntity<Resource> telechargerFichierOriginal(Long memoireId) throws IOException {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new EntityNotFoundException("Mémoire introuvable avec ID : " + memoireId));

        Path fichierPath = Paths.get(storageProperties.getUploadDir()).resolve(memoire.getFichierWord()).normalize();
        Resource resource = new UrlResource(fichierPath.toUri());

        if (!resource.exists()) {
            throw new FileNotFoundException("Fichier introuvable : " + fichierPath);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fichierPath.getFileName() + "\"")
                .body(resource);
    }

    public MemoireDTO getMemoireById(Long id) {
        Memoire memoire = memoireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mémoire non trouvé avec l'id: " + id));
        return memoireMapper.toDto(memoire);
    }


}
