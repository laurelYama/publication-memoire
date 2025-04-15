package com.esiitech.publication_memoire.entity;
import java.time.LocalDateTime;
import java.util.List;


import com.esiitech.publication_memoire.enums.StatutMemoire;
import jakarta.persistence.*;

import java.util.List;


@Table(name = "memoires")
@Entity
public class Memoire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    private String description;

    private String fichierWord;  // chemin du fichier Word
    private String fichierPdf;   // généré après validation

    @Enumerated(EnumType.STRING)
    private StatutMemoire statut = StatutMemoire.EN_ATTENTE;

    private boolean estPublic = true;

    private LocalDateTime dateSoumission = LocalDateTime.now();

    private String fichierCorrige;


    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private Utilisateur etudiant;

    @Column(name = "transmis_a_admin")
    private boolean transmisAAdmin = false;


    @ManyToOne
    @JoinColumn(name = "lecteur_id")
    private Utilisateur lecteur;

    @Column(length = 2000)
    private String commentaire;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFichierCorrige() {
        return fichierCorrige;
    }

    public void setFichierCorrige(String fichierCorrige) {
        this.fichierCorrige = fichierCorrige;
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

    public StatutMemoire getStatut() {
        return statut;
    }

    public void setStatut(StatutMemoire statut) {
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

    public Utilisateur getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Utilisateur etudiant) {
        this.etudiant = etudiant;
    }

    public Utilisateur getLecteur() {
        return lecteur;
    }

    public void setLecteur(Utilisateur lecteur) {
        this.lecteur = lecteur;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }


    public boolean isTransmisAAdmin() {
        return transmisAAdmin;
    }

    public void setTransmisAAdmin(boolean transmisAAdmin) {
        this.transmisAAdmin = transmisAAdmin;
    }
}
