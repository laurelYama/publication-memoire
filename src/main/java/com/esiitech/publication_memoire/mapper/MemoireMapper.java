package com.esiitech.publication_memoire.mapper;

import com.esiitech.publication_memoire.dto.MemoireDTO;
import com.esiitech.publication_memoire.entity.Memoire;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UtilisateurMapper.class)
public interface MemoireMapper {
    MemoireDTO toDto(Memoire memoire);
}
