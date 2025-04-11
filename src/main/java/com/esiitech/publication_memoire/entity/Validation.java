package com.esiitech.publication_memoire.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "validations")
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "memoire_id")
    private Memoire memoire;

    @Column(length = 1000)
    private String lecteurCommentaire;

    @Column(length = 1000)
    private String adminCommentaire;

    private String statut;

    private LocalDateTime dateValidation;

    // Getters & setters...

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Memoire getMemoire() { return memoire; }
    public void setMemoire(Memoire memoire) { this.memoire = memoire; }

    public String getLecteurCommentaire() { return lecteurCommentaire; }
    public void setLecteurCommentaire(String lecteurCommentaire) { this.lecteurCommentaire = lecteurCommentaire; }

    public String getAdminCommentaire() { return adminCommentaire; }
    public void setAdminCommentaire(String adminCommentaire) { this.adminCommentaire = adminCommentaire; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDateTime dateValidation) { this.dateValidation = dateValidation; }
}
