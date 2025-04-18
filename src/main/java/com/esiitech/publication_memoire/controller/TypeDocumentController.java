package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.entity.TypeDocument;
import com.esiitech.publication_memoire.service.TypeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/types")
@Tag(name = "TypeDocument", description = "Gestion des types de documents")
public class TypeDocumentController {

    private final TypeDocumentService service;

    public TypeDocumentController(TypeDocumentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer tous les types de documents", description = "Retourne la liste de tous les types de documents disponibles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des types récupérée avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    public List<TypeDocument> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer un type de document par ID", description = "Retourne un type de document spécifique selon son ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Type de document trouvé"),
            @ApiResponse(responseCode = "404", description = "Type de document non trouvé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    public TypeDocument getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un nouveau type de document", description = "Ajoute un nouveau type de document à la base de données.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Type de document créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Erreur de validation"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    public TypeDocument create(@RequestBody TypeDocument type) {
        return service.create(type);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un type de document", description = "Met à jour les informations d’un type de document existant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Type de document mis à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Type de document non trouvé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    public TypeDocument update(@PathVariable Long id, @RequestBody TypeDocument type) {
        return service.update(id, type);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un type de document", description = "Supprime un type de document selon son ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Type de document supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Type de document non trouvé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
