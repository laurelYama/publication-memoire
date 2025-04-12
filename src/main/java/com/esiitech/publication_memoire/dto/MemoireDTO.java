package com.esiitech.publication_memoire.dto;

import java.time.LocalDateTime;

public class MemoireDTO {
        private Long id;
        private String titre;
        private String description;
        private String fichierWord;
        private String urlPdf;
        private String statut;
        private boolean estPublic;
        private LocalDateTime dateSoumission;
        private UtilisateurDTO etudiant;


    // Getters & Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFichierWord() {
        return fichierWord;
    }

    public void setFichierWord(String fichierWord) {
        this.fichierWord = fichierWord;
    }

    public String getUrlPdf() {
        return urlPdf;
    }

    public void setUrlPdf(String urlPdf) {
        this.urlPdf = urlPdf;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public boolean isEstPublic() {
        return estPublic;
    }

    public void setEstPublic(boolean estPublic) {
        this.estPublic = estPublic;
    }

    public LocalDateTime getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(LocalDateTime dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public UtilisateurDTO getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(UtilisateurDTO etudiant) {
        this.etudiant = etudiant;
    }
}
