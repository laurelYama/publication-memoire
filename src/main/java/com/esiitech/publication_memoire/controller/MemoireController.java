package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.CreateMemoireDto;
import com.esiitech.publication_memoire.dto.MemoireDto;
import com.esiitech.publication_memoire.service.interfaces.MemoireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memoires")
@CrossOrigin(origins = "*")
public class MemoireController {

    @Autowired
    private MemoireService memoireService;

    @PostMapping
    public MemoireDto create(@RequestBody CreateMemoireDto dto) {
        return memoireService.create(dto);
    }

    @GetMapping
    public List<MemoireDto> getAll() {
        return memoireService.findAll();
    }

    @GetMapping("/{id}")
    public MemoireDto getById(@PathVariable Long id) {
        return memoireService.findById(id);
    }

    @GetMapping("/auteur/{auteurId}")
    public List<MemoireDto> getByAuteur(@PathVariable Long auteurId) {
        return memoireService.findByAuteur(auteurId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        memoireService.delete(id);
    }
}
