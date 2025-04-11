package com.esiitech.publication_memoire.entity;
import java.util.List;


import com.esiitech.publication_memoire.enums.StatutMemoire;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "memoires")
public class Memoire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String resume;
    private String annee;
    private String fichier;

    @Enumerated(EnumType.STRING)
    private StatutMemoire statut; // EN_ATTENTE, VALIDE, REFUSE

    @ManyToOne
    @JoinColumn(name = "auteur_id")
    private User auteur;

    @OneToMany(mappedBy = "memoire", cascade = CascadeType.ALL)
    private List<Validation> validations;

    // Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }

    public String getAnnee() { return annee; }
    public void setAnnee(String annee) { this.annee = annee; }

    public String getFichier() { return fichier; }
    public void setFichier(String fichier) { this.fichier = fichier; }

    public StatutMemoire getStatut() { return statut; }
    public void setStatut(StatutMemoire statut) { this.statut = statut; }

    public User getAuteur() { return auteur; }
    public void setAuteur(User auteur) { this.auteur = auteur; }

    public List<Validation> getValidations() { return validations; }
    public void setValidations(List<Validation> validations) { this.validations = validations; }
}
