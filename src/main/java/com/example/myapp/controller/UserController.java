package com.example.myapp.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.myapp.dto.CreateUserDTO;
import com.example.myapp.dto.UserDTO;
import com.example.myapp.entity.User;
import com.example.myapp.repository.UserRepository;
import com.example.myapp.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService,
                          UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Exemple d’endpoint pour récupérer tous les utilisateurs (uniquement pour admin, potentiellement).
     * Ici, on pourrait interdire purement et simplement cet accès pour un simple “ROLE_USER”.
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(Principal principal) {
        throw new AccessDeniedException("Accès interdit : vous ne pouvez pas lister tous les utilisateurs.");
    }

    /**
     * Récupère les infos d’un utilisateur par son ID.
     * Uniquement l’utilisateur lui-même peut appeler /api/users/{userId}.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long userId,
                                           Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur courant non trouvé"));

        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé(e) à consulter cet utilisateur.");
        }

        UserDTO dto = userService.findById(userId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Met à jour l’utilisateur (ici on supposera que l’on peut modifier seulement le username, pas l’email).
     * Seul le propriétaire (userId = ID du principal) peut modifier.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId,
                                              @Valid @RequestBody CreateUserDTO dto,
                                              Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur courant non trouvé"));

        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé(e) à modifier cet utilisateur.");
        }

        UserDTO updated = userService.update(userId, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Supprime l’utilisateur (DELETE /api/users/{userId}).
     * Seul le propriétaire peut se supprimer lui-même.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId,
                                           Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur courant non trouvé"));

        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé(e) à supprimer cet utilisateur.");
        }

        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
