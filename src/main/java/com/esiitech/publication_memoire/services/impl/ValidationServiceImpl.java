package com.esiitech.publication_memoire.services.impl;

import com.esiitech.publication_memoire.dto.CreateValidationDto;
import com.esiitech.publication_memoire.dto.ValidationDto;
import com.esiitech.publication_memoire.entity.Memoire;
import com.esiitech.publication_memoire.entity.Validation;
import com.esiitech.publication_memoire.repository.MemoireRepository;
import com.esiitech.publication_memoire.repository.ValidationRepository;
import com.esiitech.publication_memoire.services.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidationServiceImpl implements ValidationService {

    @Autowired
    private ValidationRepository validationRepository;

    @Autowired
    private MemoireRepository memoireRepository;

    @Override
    public ValidationDto valider(CreateValidationDto dto) {
        Memoire memoire = memoireRepository.findById(dto.getMemoireId())
                .orElseThrow(() -> new RuntimeException("Mémoire non trouvé"));

        Validation validation = new Validation();
        validation.setMemoire(memoire);
        validation.setLecteurCommentaire(dto.getLecteurCommentaire());
        validation.setAdminCommentaire(dto.getAdminCommentaire());
        validation.setStatut(dto.getStatut());
        validation.setDateValidation(LocalDateTime.now());

        Validation saved = validationRepository.save(validation);

        return toDto(saved);
    }

    @Override
    public List<ValidationDto> findByMemoire(Long memoireId) {
        Memoire memoire = memoireRepository.findById(memoireId)
                .orElseThrow(() -> new RuntimeException("Mémoire non trouvé"));
        return validationRepository.findByMemoire(memoire).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ValidationDto> findAll() {
        return validationRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ValidationDto toDto(Validation v) {
        ValidationDto dto = new ValidationDto();
        dto.setId(v.getId());
        dto.setMemoireId(v.getMemoire().getId());
        dto.setLecteurCommentaire(v.getLecteurCommentaire());
        dto.setAdminCommentaire(v.getAdminCommentaire());
        dto.setStatut(v.getStatut());
        dto.setDateValidation(v.getDateValidation());
        return dto;
    }
}
