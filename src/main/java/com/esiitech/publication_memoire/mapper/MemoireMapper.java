package com.esiitech.publication_memoire.mapper;

import com.esiitech.publication_memoire.dto.MemoireDTO;
import com.esiitech.publication_memoire.entity.Memoire;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = UtilisateurMapper.class)
public interface MemoireMapper {
    MemoireDTO toDto(Memoire memoire);

    List<MemoireDTO> toDtoList(List<Memoire> memoires);
}
