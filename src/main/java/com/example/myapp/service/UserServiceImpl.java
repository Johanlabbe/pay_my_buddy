package com.example.myapp.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.myapp.dto.CreateUserDTO;
import com.example.myapp.dto.UserDTO;
import com.example.myapp.entity.User;
import com.example.myapp.repository.UserRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    public UserDTO create(CreateUserDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRole("USER");
        User saved = userRepository.save(user);
        return UserDTO.fromEntity(saved);
    }

    @Override
    public UserDTO update(Long userId, CreateUserDTO dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable pour l'id : " + userId));
        user.setUsername(dto.getUsername());
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
}
