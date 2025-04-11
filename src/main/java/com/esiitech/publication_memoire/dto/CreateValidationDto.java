package com.esiitech.publication_memoire.dto;

public class CreateValidationDto {
    private Long memoireId;
    private String lecteurCommentaire;
    private String adminCommentaire;
    private String statut;

    // Getters & setters
    public Long getMemoireId() { return memoireId; }
    public void setMemoireId(Long memoireId) { this.memoireId = memoireId; }

    public String getLecteurCommentaire() { return lecteurCommentaire; }
    public void setLecteurCommentaire(String lecteurCommentaire) { this.lecteurCommentaire = lecteurCommentaire; }

    public String getAdminCommentaire() { return adminCommentaire; }
    public void setAdminCommentaire(String adminCommentaire) { this.adminCommentaire = adminCommentaire; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
