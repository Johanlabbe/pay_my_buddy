package com.example.myapp.service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.myapp.dto.CreateUserDTO;
import com.example.myapp.dto.UpdateProfileDTO;
import com.example.myapp.dto.UserDTO;
import com.example.myapp.entity.User;
import com.example.myapp.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* ---------- Lecture ---------- */

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable pour l'id : " + id));
        return UserDTO.fromEntity(user);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable pour l'email : " + email));
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<User> findByEmailOpt(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Utilisateur non authentifié.");
        }
        String email = authentication.getName();
        if (email == null || "anonymousUser".equalsIgnoreCase(email)) {
            throw new IllegalStateException("Utilisateur non authentifié.");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur authentifié introuvable."));
    }

    /* ---------- Création / Mise à jour / Suppression ---------- */

    @Override
    public UserDTO create(CreateUserDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        String username = dto.getUsername().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé.");
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("USER");
        user.setBalance(BigDecimal.ZERO);

        User saved = userRepository.save(user);
        return UserDTO.fromEntity(saved);
    }

    @Override
    public UserDTO update(Long userId, CreateUserDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable pour l'id : " + userId));
        user.setUsername(dto.getUsername().trim());
        User updated = userRepository.save(user);
        return UserDTO.fromEntity(updated);
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("Utilisateur introuvable pour l'id : " + id);
        }
        userRepository.deleteById(id);
    }

    /* ---------- Profil & Mot de passe ---------- */

    @Override
    public UserDTO updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable pour l'id : " + userId));

        String newEmail = dto.getEmail().trim().toLowerCase();
        String newUsername = dto.getUsername().trim();

        Optional<User> byEmail = userRepository.findByEmail(newEmail);
        if (byEmail.isPresent() && !byEmail.get().getId().equals(userId)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        Optional<User> byUsername = userRepository.findByUsername(newUsername);
        if (byUsername.isPresent() && !byUsername.get().getId().equals(userId)) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé.");
        }

        user.setEmail(newEmail);
        user.setUsername(newUsername);

        User saved = userRepository.save(user);
        return UserDTO.fromEntity(saved);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable pour l'id : " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
