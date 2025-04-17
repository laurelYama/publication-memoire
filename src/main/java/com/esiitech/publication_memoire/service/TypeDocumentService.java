package com.esiitech.publication_memoire.service;

import com.esiitech.publication_memoire.entity.TypeDocument;
import com.esiitech.publication_memoire.repository.TypeDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TypeDocumentService {
    private final TypeDocumentRepository repository;

    public TypeDocumentService(TypeDocumentRepository repository) {
        this.repository = repository;
    }

    public List<TypeDocument> findAll() {
        return repository.findAll();
    }

    public TypeDocument findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Type non trouvé"));
    }

    public TypeDocument create(TypeDocument type) {
        return repository.save(type);
    }

    public TypeDocument update(Long id, TypeDocument updated) {
        TypeDocument type = findById(id);
        type.setNom(updated.getNom());
        return repository.save(type);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}

