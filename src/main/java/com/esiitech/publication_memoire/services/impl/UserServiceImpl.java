package com.esiitech.publication_memoire.services.impl;

import com.esiitech.publication_memoire.dto.RegisterRequestDto;
import com.esiitech.publication_memoire.dto.UserDto;
import com.esiitech.publication_memoire.entity.User;
import com.esiitech.publication_memoire.enums.Role;
import com.esiitech.publication_memoire.repository.UserRepository;
import com.esiitech.publication_memoire.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDto register(RegisterRequestDto request) {
        User user = new User();
        user.setNom(request.getNom());
        user.setEmail(request.getEmail());
        user.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.ETUDIANT);

        User saved = userRepository.save(user);

        UserDto dto = new UserDto();
        dto.setId(saved.getId());
        dto.setNom(saved.getNom());
        dto.setEmail(saved.getEmail());
        dto.setRole(saved.getRole());
        return dto;
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setNom(user.getNom());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
