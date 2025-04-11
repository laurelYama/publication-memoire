package com.esiitech.publication_memoire.service.interfaces;

import com.esiitech.publication_memoire.dto.CreateMemoireDto;
import com.esiitech.publication_memoire.dto.MemoireDto;

import java.util.List;

public interface MemoireService {
    MemoireDto create(CreateMemoireDto dto);
    List<MemoireDto> findAll();
    MemoireDto findById(Long id);
    List<MemoireDto> findByAuteur(Long userId);
    void delete(Long id);
}
