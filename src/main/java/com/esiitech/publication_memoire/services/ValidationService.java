package com.esiitech.publication_memoire.services;

import com.esiitech.publication_memoire.dto.CreateValidationDto;
import com.esiitech.publication_memoire.dto.ValidationDto;

import java.util.List;

public interface ValidationService {
    ValidationDto valider(CreateValidationDto dto);
    List<ValidationDto> findByMemoire(Long memoireId);
    List<ValidationDto> findAll();
}
