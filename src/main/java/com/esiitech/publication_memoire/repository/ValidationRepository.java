package com.esiitech.publication_memoire.repository;

import com.esiitech.publication_memoire.entity.Validation;
import com.esiitech.publication_memoire.entity.Memoire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ValidationRepository extends JpaRepository<Validation, Long> {
    List<Validation> findByMemoire(Memoire memoire);
}
