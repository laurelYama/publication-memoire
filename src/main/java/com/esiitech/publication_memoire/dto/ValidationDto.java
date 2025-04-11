package com.esiitech.publication_memoire.dto;

import java.time.LocalDateTime;

public class ValidationDto {
    private Long id;
    private Long memoireId;
    private String lecteurCommentaire;
    private String adminCommentaire;
    private String statut;
    private LocalDateTime dateValidation;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemoireId() { return memoireId; }
    public void setMemoireId(Long memoireId) { this.memoireId = memoireId; }

    public String getLecteurCommentaire() { return lecteurCommentaire; }
    public void setLecteurCommentaire(String lecteurCommentaire) { this.lecteurCommentaire = lecteurCommentaire; }

    public String getAdminCommentaire() { return adminCommentaire; }
    public void setAdminCommentaire(String adminCommentaire) { this.adminCommentaire = adminCommentaire; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDateTime dateValidation) { this.dateValidation = dateValidation; }
}
