package com.esiitech.publication_memoire.services;

import com.esiitech.publication_memoire.dto.UserDto;
import com.esiitech.publication_memoire.dto.RegisterRequestDto;
import java.util.List;

public interface UserService {
    UserDto register(RegisterRequestDto request);
    UserDto getUserById(Long id);
    List<UserDto> getAllUsers();
    void deleteUser(Long id);
}
