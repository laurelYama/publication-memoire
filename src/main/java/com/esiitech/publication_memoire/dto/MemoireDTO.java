package com.esiitech.publication_memoire.dto;

import java.time.LocalDateTime;

public class MemoireDTO {
        private Long id;
        private String titre;
        private String description;
        private String fichierWord;
        private String fichierPdf;
        private String statut;
        private boolean estPublic;
        private LocalDateTime dateSoumission;
        private UtilisateurDTO etudiant;
        private String typeDocumentNom;
        private Long typeDocumentId;





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

    public String getFichierPdf() {
        return fichierPdf;
    }

    public void setFichierPdf(String fichierPdf) {
        this.fichierPdf = fichierPdf;
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

    public String getTypeDocumentNom() {
        return typeDocumentNom;
    }

    public void setTypeDocumentNom(String typeDocumentNom) {
        this.typeDocumentNom = typeDocumentNom;
    }

    public Long getTypeDocumentId() {
        return typeDocumentId;
    }

    public void setTypeDocumentId(Long typeDocumentId) {
        this.typeDocumentId = typeDocumentId;
    }
}
