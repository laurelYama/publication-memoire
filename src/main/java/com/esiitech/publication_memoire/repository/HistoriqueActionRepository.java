package com.esiitech.publication_memoire.repository;


import com.esiitech.publication_memoire.entity.HistoriqueAction;
import com.esiitech.publication_memoire.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueActionRepository extends JpaRepository<HistoriqueAction, Long> {
    List<HistoriqueAction> findByUtilisateur(Utilisateur utilisateur);
}

