package com.esiitech.publication_memoire.repository;

import com.esiitech.publication_memoire.entity.Memoire;
import com.esiitech.publication_memoire.entity.User;
import com.esiitech.publication_memoire.enums.StatutMemoire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemoireRepository extends JpaRepository<Memoire, Long> {
    List<Memoire> findByAuteur(User auteur);
    List<Memoire> findByStatut(StatutMemoire statut);
}
