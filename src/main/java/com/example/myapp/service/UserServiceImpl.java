package com.example.myapp.service;

import com.example.myapp.dto.CreateUserDTO;
import com.example.myapp.dto.UserDTO;
import com.example.myapp.entity.User;
import com.example.myapp.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

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

    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable pour l'id : " + id));
        return UserDTO.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable pour l'email : " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmailOpt(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserDTO create(CreateUserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé.");
        }

        User user = new User();
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setUsername(dto.getUsername().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("USER");

        User saved = userRepository.save(user);
        return UserDTO.fromEntity(saved);
    }

    @Override
    public UserDTO update(Long userId, CreateUserDTO dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable pour l'id : " + userId));

        user.setUsername(dto.getUsername());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

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

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found: " + email));
    }
}
