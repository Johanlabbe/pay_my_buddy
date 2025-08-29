package com.example.myapp.service;

import com.example.myapp.dto.CreateUserDTO;
import com.example.myapp.dto.UserDTO;
import com.example.myapp.entity.User;
import org.springframework.security.core.Authentication;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Contrat du service utilisateur.
 */
public interface UserService {

    /**
     * Crée un nouvel utilisateur.
     */
    UserDTO create(CreateUserDTO dto);

    /**
     * Met à jour l’utilisateur identifié par userId.
     */
    UserDTO update(Long userId, CreateUserDTO dto);

    /**
     * Supprime l’utilisateur identifié par id.
     */
    void delete(Long id);

    /**
     * Retourne les données de l’utilisateur identifié par id.
     */
    UserDTO findById(Long id);

    /**
     * Retourne l’utilisateur (entity) identifié par son email.
     * @throws NoSuchElementException si introuvable
     */
    User findByEmail(String email);

    /**
     * Variant optionnel pour ne pas lever d’exception.
     */
    Optional<User> findByEmailOpt(String email);

    /**
     * Retourne l'utilisateur courant à partir du contexte d'authentification.
     * @throws IllegalStateException si non authentifié
     * @throws IllegalArgumentException si l'utilisateur authentifié est introuvable
     */
    User getCurrentUser(Authentication authentication);
}
