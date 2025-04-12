package com.esiitech.publication_memoire.mapper;

import com.esiitech.publication_memoire.dto.UtilisateurDTO;
import com.esiitech.publication_memoire.entity.Utilisateur;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UtilisateurMapper {
    UtilisateurDTO toDto(Utilisateur utilisateur);
}
