package com.esiitech.publication_memoire.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class HistoriqueAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action; // Exemple : "Soumission mémoire", "Rejet", etc.

    private LocalDateTime dateAction;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "memoire_id", nullable = true)
    private Memoire memoire; // Peut être null pour des actions générales

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Memoire getMemoire() {
        return memoire;
    }

    public void setMemoire(Memoire memoire) {
        this.memoire = memoire;
    }
}

