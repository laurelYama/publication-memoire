package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.RegisterRequestDto;
import com.esiitech.publication_memoire.dto.UserDto;
import com.esiitech.publication_memoire.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // à adapter si besoin
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public UserDto register(@RequestBody RegisterRequestDto request) {
        return userService.register(request);
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
