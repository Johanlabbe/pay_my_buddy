package com.example.myapp.service;

import com.example.myapp.dto.CreateUserDTO;
import com.example.myapp.dto.UserDTO;

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
    com.example.myapp.entity.User findByEmail(String email);
}
