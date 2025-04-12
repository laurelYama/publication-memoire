package com.esiitech.publication_memoire.service.implementations;

import com.esiitech.publication_memoire.dto.CreateMemoireDto;
import com.esiitech.publication_memoire.dto.MemoireDto;
import com.esiitech.publication_memoire.entity.Memoire;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.StatutMemoire;
import com.esiitech.publication_memoire.repository.MemoireRepository;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import com.esiitech.publication_memoire.service.interfaces.MemoireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemoireServiceImpl implements MemoireService {

    @Autowired
    private MemoireRepository memoireRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    public MemoireDto create(CreateMemoireDto dto) {
        Utilisateur auteur = utilisateurRepository.findById(dto.getAuteurId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auteur introuvable"));

        Memoire memoire = new Memoire();
        memoire.setTitre(dto.getTitre());
        memoire.setResume(dto.getResume());
        memoire.setAnnee(dto.getAnnee());
        memoire.setFichier(dto.getFichier());
        memoire.setAuteur(auteur);
        memoire.setStatut(StatutMemoire.EN_ATTENTE);

        Memoire saved = memoireRepository.save(memoire);

        return toDto(saved);
    }


    @Override
    public List<MemoireDto> findAll() {
        return memoireRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MemoireDto findById(Long id) {
        Memoire memoire = memoireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mémoire non trouvé"));
        return toDto(memoire);
    }

    @Override
    public List<MemoireDto> findByAuteur(Long userId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Auteur introuvable"));
        return memoireRepository.findByAuteur(utilisateur).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        memoireRepository.deleteById(id);
    }

    private MemoireDto toDto(Memoire m) {
        MemoireDto dto = new MemoireDto();
        dto.setId(m.getId());
        dto.setTitre(m.getTitre());
        dto.setResume(m.getResume());
        dto.setAnnee(m.getAnnee());
        dto.setFichier(m.getFichier());
        dto.setStatut(m.getStatut());
        dto.setAuteurId(m.getAuteur().getId());
        dto.setAuteurNom(m.getAuteur().getNom());
        return dto;
    }
}
