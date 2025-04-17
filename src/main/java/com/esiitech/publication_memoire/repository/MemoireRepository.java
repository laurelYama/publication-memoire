package com.esiitech.publication_memoire.repository;


import com.esiitech.publication_memoire.entity.Memoire;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.StatutMemoire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemoireRepository extends JpaRepository<Memoire, Long> {
    List<Memoire> findAll();

    @Query("""
    SELECT m FROM Memoire m
    WHERE m.estPublic = true
      AND (:titre IS NULL OR LOWER(m.titre) LIKE LOWER(CONCAT('%', :titre, '%')))
      AND (:nom IS NULL OR LOWER(m.etudiant.nom) LIKE LOWER(CONCAT('%', :nom, '%')))
      AND (:prenom IS NULL OR LOWER(m.etudiant.prenom) LIKE LOWER(CONCAT('%', :prenom, '%')))
      AND (:typeDocumentId IS NULL OR m.type.id = :typeDocumentId)
      AND (:typeDocumentNom IS NULL OR LOWER(m.type.nom) LIKE LOWER(CONCAT('%', :typeDocumentNom, '%')))
""")
    List<Memoire> chercherMemoiresPublics(
            @Param("titre") String titre,
            @Param("nom") String nom,
            @Param("prenom") String prenom,
            @Param("typeDocumentId") Long typeDocumentId,
            @Param("typeDocumentNom") String typeDocumentNom
    );





    @Query("""
    SELECT m FROM Memoire m
    WHERE (:titre IS NULL OR LOWER(m.titre) LIKE LOWER(CONCAT('%', :titre, '%')))
      AND (:nom IS NULL OR LOWER(m.etudiant.nom) LIKE LOWER(CONCAT('%', :nom, '%')))
      AND (:prenom IS NULL OR LOWER(m.etudiant.prenom) LIKE LOWER(CONCAT('%', :prenom, '%')))
      AND (:statut IS NULL OR m.statut = :statut)
      AND (:estPublic IS NULL OR m.estPublic = :estPublic)
      AND (:typeDocumentId IS NULL OR m.type.id = :typeDocumentId)
      AND (:typeDocumentNom IS NULL OR LOWER(m.type.nom) LIKE LOWER(CONCAT('%', :typeDocumentNom, '%')))
""")
    List<Memoire> rechercheFlexible(
            @Param("titre") String titre,
            @Param("nom") String nom,
            @Param("prenom") String prenom,
            @Param("statut") StatutMemoire statut,
            @Param("estPublic") Boolean estPublic,
            @Param("typeDocumentId") Long typeDocumentId,
            @Param("typeDocumentNom") String typeDocumentNom
    );



    @Query("""
    SELECT m FROM Memoire m
    WHERE (:titre IS NULL OR LOWER(m.titre) LIKE LOWER(CONCAT('%', :titre, '%')))
      AND (:nom IS NULL OR LOWER(m.etudiant.nom) LIKE LOWER(CONCAT('%', :nom, '%')))
      AND (:prenom IS NULL OR LOWER(m.etudiant.prenom) LIKE LOWER(CONCAT('%', :prenom, '%')))
      AND (:typeDocumentId IS NULL OR m.type.id = :typeDocumentId)
      AND (:typeDocumentNom IS NULL OR LOWER(m.type.nom) LIKE LOWER(CONCAT('%', :typeDocumentNom, '%')))
      AND m.statut = 'VALIDE'
""")
    List<Memoire> rechercherMemoiresValides(
            @Param("titre") String titre,
            @Param("nom") String nom,
            @Param("prenom") String prenom,
            @Param("typeDocumentId") Long typeDocumentId,
            @Param("typeDocumentNom") String typeDocumentNom
    );





    Long countByEtudiantAndStatut(Utilisateur etudiant, StatutMemoire statut);
    long countByLecteur(Utilisateur lecteur);
    long countByLecteurAndTransmisAAdminTrue(Utilisateur lecteur);
    Long countByStatut(StatutMemoire statut);



}
