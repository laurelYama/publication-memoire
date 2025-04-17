package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.entity.TypeDocument;
import com.esiitech.publication_memoire.service.TypeDocumentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/types")
public class TypeDocumentController {

    private final TypeDocumentService service;

    public TypeDocumentController(TypeDocumentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<TypeDocument> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TypeDocument getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TypeDocument create(@RequestBody TypeDocument type) {
        return service.create(type);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TypeDocument update(@PathVariable Long id, @RequestBody TypeDocument type) {
        return service.update(id, type);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
