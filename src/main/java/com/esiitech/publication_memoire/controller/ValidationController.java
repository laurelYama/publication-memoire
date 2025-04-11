package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.CreateValidationDto;
import com.esiitech.publication_memoire.dto.ValidationDto;
import com.esiitech.publication_memoire.services.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/validations")
@CrossOrigin(origins = "*")
public class ValidationController {

    @Autowired
    private ValidationService validationService;

    @PostMapping
    public ValidationDto valider(@RequestBody CreateValidationDto dto) {
        return validationService.valider(dto);
    }

    @GetMapping
    public List<ValidationDto> getAll() {
        return validationService.findAll();
    }

    @GetMapping("/memoire/{memoireId}")
    public List<ValidationDto> getByMemoire(@PathVariable Long memoireId) {
        return validationService.findByMemoire(memoireId);
    }
}
