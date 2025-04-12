package com.esiitech.publication_memoire.service;

import com.esiitech.publication_memoire.dto.MemoireDTO;
import com.esiitech.publication_memoire.entity.Memoire;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.Role;
import com.esiitech.publication_memoire.enums.StatutMemoire;
import com.esiitech.publication_memoire.mapper.MemoireMapper;
import com.esiitech.publication_memoire.repository.MemoireRepository;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.convert.out.pdf.PdfConversion;
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class MemoireService {

    private final MemoireRepository memoireRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailService emailService;
    private final MemoireMapper memoireMapper;

    private final String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";

    public MemoireService(
            MemoireRepository memoireRepository,
            UtilisateurRepository utilisateurRepository,
            EmailService emailService,
            MemoireMapper memoireMapper) {
        this.memoireRepository = memoireRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.emailService = emailService;
        this.memoireMapper = memoireMapper;
    }

    public MemoireDTO soumettreMemoire(Long etudiantId, String titre, String description, MultipartFile fichierWord) throws IOException {
        Utilisateur etudiant = verifierEtudiant(etudiantId);
        String cheminFichier = sauvegarderFichier(fichierWord);

        Memoire memoire = creerMemoire(titre, description, cheminFichier, etudiant);
        Memoire savedMemoire = memoireRepository.save(memoire);

        return memoireMapper.toDto(savedMemoire);
    }

    private Utilisateur verifierEtudiant(Long etudiantId) {
        return utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));
    }

    private String sauvegarderFichier(MultipartFile fichierWord) throws IOException {
        if (fichierWord.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }

        String originalFilename = fichierWord.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".doc") && !originalFilename.toLowerCase().endsWith(".docx"))) {
            throw new RuntimeException("Le fichier doit être au format Word (.doc ou .docx)");
        }

        String nomUnique = UUID.randomUUID().toString() + "_" + originalFilename;
        File dossier = new File(uploadDir);
        if (!dossier.exists()) {
            dossier.mkdirs();
        }

        String cheminFichier = Paths.get(uploadDir, nomUnique).toString();
        fichierWord.transferTo(new File(cheminFichier));

        return cheminFichier;
    }

    private Memoire creerMemoire(String titre, String description, String cheminFichier, Utilisateur etudiant) {
        Memoire memoire = new Memoire();
        memoire.setTitre(titre);
        memoire.setDescription(description);
        memoire.setFichierWord(cheminFichier);
        memoire.setEtudiant(etudiant);
        memoire.setStatut(StatutMemoire.EN_ATTENTE);
        return memoire;
    }



    public MemoireDTO transmettreParLecteur(Long memoireId, Long lecteurId, String commentaire) {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new RuntimeException("Mémoire introuvable"));

        Utilisateur lecteur = utilisateurRepository.findById(lecteurId)
                .orElseThrow(() -> new RuntimeException("Lecteur introuvable"));

        memoire.setLecteur(lecteur);
        memoire.setStatut(StatutMemoire.TRANSMIS);
        memoire.setCommentaire(commentaire);

        Utilisateur admin = utilisateurRepository.findByRole(Role.ADMIN)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Administrateur introuvable"));

        String message = String.format(
                "Bonjour,\n\nLe mémoire intitulé \"%s\" a été transmis par %s %s.\n\nCommentaire : %s",
                memoire.getTitre(),
                lecteur.getPrenom(),
                lecteur.getNom(),
                commentaire != null ? commentaire : "Aucun commentaire"
        );

        emailService.sendEmail(
                admin.getEmail(),
                "Mémoire transmis par le lecteur",
                message
        );

        Memoire updated = memoireRepository.save(memoire);

        // Appel correct via l'instance injectée
        return memoireMapper.toDto(updated);
    }


    public MemoireDTO getMemoireById(Long id) {
        Memoire memoire = memoireRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mémoire introuvable avec l'id : " + id));
        return memoireMapper.toDto(memoire);
    }

    public Memoire validerParAdmin(Long memoireId) {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new RuntimeException("Mémoire introuvable"));

        memoire.setStatut(StatutMemoire.VALIDE);

        try {
            String pdfPath = convertirWordEnPdf(memoire.getFichierWord());
            memoire.setFichierPdf(pdfPath);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la conversion en PDF : " + e.getMessage());
        }

        Utilisateur etudiant = memoire.getEtudiant();
        emailService.sendEmail(
                etudiant.getEmail(),
                "Validation de votre mémoire",
                "Bonjour " + etudiant.getPrenom() + ",\n\nVotre mémoire intitulé \"" + memoire.getTitre() + "\" a été validé avec succès.\n\nCordialement,\nL'équipe."
        );

        return memoireRepository.save(memoire);
    }

    public MemoireDTO rejeterParAdmin(Long memoireId, String commentaire) {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new RuntimeException("Mémoire introuvable"));

        memoire.setStatut(StatutMemoire.REJETE);
        memoire.setCommentaire(commentaire);

        Utilisateur etudiant = memoire.getEtudiant();
        emailService.sendEmail(
                etudiant.getEmail(),
                "Rejet de votre mémoire",
                "Bonjour " + etudiant.getPrenom() + ",\n\nVotre mémoire intitulé \"" + memoire.getTitre() + "\" a été rejeté pour la raison suivante :\n" +
                        commentaire + "\n\nMerci de corriger et soumettre à nouveau.\n\nCordialement,\nL'équipe."
        );

        Memoire updated = memoireRepository.save(memoire);

        // Utilisation de MapStruct pour convertir l'entité en DTO
        return memoireMapper.toDto(updated); // On utilise directement le mapper injecté
    }




    public MemoireDTO changerVisibilite(Long memoireId, boolean estPublic) {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new RuntimeException("Mémoire introuvable"));

        memoire.setEstPublic(estPublic);
        Memoire updated = memoireRepository.save(memoire);

        return memoireMapper.toDto(updated);
    }


    public List<Memoire> getMemoiresPublics() {
        return memoireRepository.findByEstPublicTrueAndStatut(StatutMemoire.VALIDE);
    }

    public Resource getFichierMemoire(Long memoireId) {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new RuntimeException("Mémoire introuvable avec ID : " + memoireId));

        Path fichierPath = Paths.get(memoire.getFichierWord()).normalize();
        try {
            Resource resource = new UrlResource(fichierPath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Fichier non trouvé");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur lors du chargement du fichier", e);
        }
    }

    public static String convertirWordEnPdf(String cheminFichierWord) throws Docx4JException, IOException {
        // Vérifie que le fichier existe
        File fichierWord = new File(cheminFichierWord);
        if (!fichierWord.exists()) {
            throw new FileNotFoundException("Fichier Word introuvable : " + cheminFichierWord);
        }

        // Charge le document Word
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(fichierWord);
        PdfConversion conversion = new Conversion(wordMLPackage);

        // Crée le dossier de sortie s'il n'existe pas
        String dossierPdf = "pdfs";
        File dossier = new File(dossierPdf);
        if (!dossier.exists()) {
            dossier.mkdirs();
        }

        // Remplace l'extension par .pdf (insensible à la casse, .doc ou .docx)
        String nomFichierPdf = fichierWord.getName().replaceAll("(?i)\\.docx?$", ".pdf");

        // Construit le chemin complet du PDF
        String cheminFichierPdf = Paths.get(dossierPdf, nomFichierPdf).toString();

        // Écrit le PDF
        try (FileOutputStream outputStream = new FileOutputStream(cheminFichierPdf)) {
            conversion.output(outputStream, null);
            System.out.println("Conversion réussie : " + cheminFichierPdf);
        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion : " + e.getMessage());
            throw e;
        }

        return cheminFichierPdf;
    }
}
