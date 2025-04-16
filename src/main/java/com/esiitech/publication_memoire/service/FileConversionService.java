package com.esiitech.publication_memoire.service;

import com.esiitech.publication_memoire.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileConversionService {

    private final StorageProperties storageProperties;
    private final FileStorageService fileStorageService;

    public boolean isLibreOfficeAvailable() {
        try {
            File soffice = new File(storageProperties.getSofficePath());
            boolean exists = soffice.exists() && soffice.canExecute();
            log.info("LibreOffice disponible : {}", exists);
            return exists;
        } catch (Exception e) {
            log.error("Erreur lors de la vérification de LibreOffice : {}", e.getMessage());
            return false;
        }
    }

    public String convertirWordEnPdf(String cheminRelatifFichierWord) throws IOException, InterruptedException {
        Path cheminAbsolu = fileStorageService.getCheminAbsolu(cheminRelatifFichierWord)
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
}
