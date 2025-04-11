package com.esiitech.publication_memoire.repository;

import com.esiitech.publication_memoire.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findByActivationToken(String token);
    boolean existsByEmail(String email);
}

