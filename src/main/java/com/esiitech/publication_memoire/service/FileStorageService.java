package com.esiitech.publication_memoire.service;

import com.esiitech.publication_memoire.config.StorageProperties;
import com.esiitech.publication_memoire.exception.FileStorageException;
import com.esiitech.publication_memoire.exception.FileNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path baseLocation;
    private final StorageProperties storageProperties;

    public FileStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.baseLocation = Paths.get(storageProperties.getBasePath());
        try {
            Files.createDirectories(baseLocation);
        } catch (IOException e) {
            throw new FileStorageException("Impossible de créer le répertoire de stockage", e);
        }
    }

    /**
     * Sauvegarde un fichier dans un sous-dossier spécifié
     *
     * @param fichier Le fichier à sauvegarder
     * @param dossier Le sous-dossier de destination
     * @param mimeTypesAcceptes Les types MIME acceptés
     * @return Le chemin relatif du fichier sauvegardé
     */
    public String sauvegarderFichier(MultipartFile fichier, String dossier, String... mimeTypesAcceptes) {
        if (fichier == null || fichier.isEmpty()) {
            throw new FileStorageException("Le fichier ne peut pas être vide");
        }

        // Vérification du type MIME
        String typeMime = fichier.getContentType();
        boolean mimeTypeValide = false;

        if (typeMime != null && mimeTypesAcceptes.length > 0) {
            for (String mimeAccepte : mimeTypesAcceptes) {
                if (typeMime.equals(mimeAccepte)) {
                    mimeTypeValide = true;
                    break;
                }
            }

            if (!mimeTypeValide) {
                throw new FileStorageException("Type de fichier non autorisé: " + typeMime);
            }
        }

        try {
            // Création du dossier si nécessaire
            Path dossierPath = baseLocation.resolve(dossier);
            Files.createDirectories(dossierPath);

            // Génération d'un nom unique
            String originalFilename = fichier.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String nomUnique = UUID.randomUUID() + extension;

            Path destinationFile = dossierPath.resolve(nomUnique);

            // Sauvegarde du fichier
            try (InputStream inputStream = fichier.getInputStream()) {
                Files.copy(inputStream, destinationFile);
            }

            return dossier + "/" + nomUnique;
        } catch (IOException e) {
            throw new FileStorageException("Erreur lors de la sauvegarde du fichier " + fichier.getOriginalFilename(), e);
        }
    }

    /**
     * Charge un fichier comme ressource
     */
    public Resource chargerFichier(String cheminRelatif) {
        try {
            Path filePath = baseLocation.resolve(cheminRelatif);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Fichier introuvable: " + cheminRelatif);
            }
        } catch (IOException e) {
            throw new FileNotFoundException("Erreur lors du chargement du fichier: " + cheminRelatif, e);
        }
    }

    /**
     * Supprime un fichier
     */
    public void supprimerFichier(String cheminRelatif) {
        try {
            Path filePath = baseLocation.resolve(cheminRelatif);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Erreur lors de la suppression du fichier: " + cheminRelatif, e);
        }
    }

    /**
     * Obtient le chemin absolu d'un fichier
     */
    public Path getCheminAbsolu(String cheminRelatif) {
        return baseLocation.resolve(cheminRelatif);
    }
}