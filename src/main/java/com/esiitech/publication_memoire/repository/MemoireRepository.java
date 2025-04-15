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

    // Pour admin : tout, filtré par titre
    @Query("SELECT m FROM Memoire m WHERE (:titre IS NULL OR LOWER(m.titre) LIKE LOWER(CONCAT('%', :titre, '%')))")
    List<Memoire> findByTitre(@Param("titre") String titre);

    // Pour utilisateur non connecté : statut = VALIDE + estPublic = true
    List<Memoire> findByStatutAndEstPublicAndTitreContainingIgnoreCase(StatutMemoire statut, boolean estPublic, String titre);


    Long countByEtudiantAndStatut(Utilisateur etudiant, StatutMemoire statut);
    long countByLecteurAndStatutAndTransmisAAdminFalse(Utilisateur lecteur, StatutMemoire statut);
    long countByLecteurAndStatutAndTransmisAAdminTrue(Utilisateur lecteur, StatutMemoire statut);
    Long countByStatut(StatutMemoire statut);


    // Admin
    @Query("SELECT m FROM Memoire m WHERE LOWER(m.etudiant.nom) LIKE LOWER(CONCAT('%', :motCle, '%')) OR LOWER(m.etudiant.prenom) LIKE LOWER(CONCAT('%', :motCle, '%'))")
    List<Memoire> searchByNomOrPrenomEtudiant(@Param("motCle") String motCle);


    // Étudiant
    @Query("SELECT m FROM Memoire m WHERE (LOWER(m.etudiant.nom) LIKE LOWER(CONCAT('%', :motCle, '%')) OR LOWER(m.etudiant.prenom) LIKE LOWER(CONCAT('%', :motCle, '%'))) AND m.statut = :statut")
    List<Memoire> searchByNomOrPrenomEtudiantAndStatut(@Param("motCle") String motCle, @Param("statut") StatutMemoire statut);


    // Non connecté
    @Query("SELECT m FROM Memoire m WHERE (LOWER(m.etudiant.nom) LIKE LOWER(CONCAT('%', :motCle, '%')) OR LOWER(m.etudiant.prenom) LIKE LOWER(CONCAT('%', :motCle, '%'))) AND m.statut = :statut AND m.estPublic = :estPublic")
    List<Memoire> searchByNomOrPrenomEtudiantAndStatutAndEstPublic(@Param("motCle") String motCle, @Param("statut") StatutMemoire statut, @Param("estPublic") boolean estPublic);





    List<Memoire> findByTitreContainingIgnoreCase(String titre);
    List<Memoire> findByStatutAndTitreContainingIgnoreCase(StatutMemoire statut, String titre);
    List<Memoire> findByEstPublicAndTitreContainingIgnoreCase(Boolean estPublic, String titre);
    List<Memoire> findByStatutAndEstPublicAndTitreContainingIgnoreCase(StatutMemoire statut, Boolean estPublic, String titre);
}
