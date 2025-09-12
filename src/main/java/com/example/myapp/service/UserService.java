package com.example.myapp.service;

import com.example.myapp.dto.CreateUserDTO;
import com.example.myapp.dto.UpdateProfileDTO;
import com.example.myapp.dto.UserDTO;
import com.example.myapp.entity.User;
import org.springframework.security.core.Authentication;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Service métier utilisateur (CRUD + profil + auth).
 */
public interface UserService {

    /**
     * Crée un nouvel utilisateur.
     * @param dto données (username, email, password)
     * @return l'utilisateur créé
     * @throws IllegalArgumentException email/username déjà utilisés
     */
    UserDTO create(CreateUserDTO dto);

    /**
     * Met à jour un utilisateur existant.
     * @param userId id de l'utilisateur
     * @param dto valeurs à appliquer
     * @return l'utilisateur mis à jour
     * @throws NoSuchElementException si introuvable
     * @throws IllegalArgumentException valeurs invalides (ex. unicité)
     */
    UserDTO update(Long userId, CreateUserDTO dto);

    /**
     * Supprime un utilisateur.
     * @param id id de l'utilisateur
     * @throws NoSuchElementException si introuvable
     */
    void delete(Long id);

    /**
     * Retourne un utilisateur par id.
     * @param id id recherché
     * @return l'utilisateur
     * @throws NoSuchElementException si introuvable
     */
    UserDTO findById(Long id);

    /**
     * Retourne l'entité par email.
     * @param email email recherché
     * @return l'entité utilisateur
     * @throws NoSuchElementException si introuvable
     */
    User findByEmail(String email);

    /**
     * Variante optionnelle de recherche par email.
     * @param email email recherché
     * @return utilisateur ou vide
     */
    Optional<User> findByEmailOpt(String email);

    /**
     * Retourne l'utilisateur courant depuis l'authentification.
     * @param authentication contexte Spring Security
     * @return entité utilisateur authentifiée
     * @throws IllegalStateException si non authentifié
     * @throws IllegalArgumentException si utilisateur introuvable
     */
    User getCurrentUser(Authentication authentication);

    /**
     * Met à jour le profil (email/username).
     * @param userId id de l'utilisateur
     * @param dto nouvelles valeurs
     * @return profil mis à jour
     * @throws NoSuchElementException si introuvable
     * @throws IllegalArgumentException email/username déjà utilisés
     */
    UserDTO updateProfile(Long userId, UpdateProfileDTO dto);

    /**
     * Change le mot de passe.
     * @param userId id de l'utilisateur
     * @param currentPassword mot de passe actuel (clair)
     * @param newPassword nouveau mot de passe (clair)
     * @throws NoSuchElementException si introuvable
     * @throws IllegalArgumentException mot de passe actuel incorrect ou nouveau invalide
     */
    void changePassword(Long userId, String currentPassword, String newPassword);
}
