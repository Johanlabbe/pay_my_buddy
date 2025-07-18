package com.example.myapp.repository;

import com.example.myapp.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Recherche un utilisateur par son email.
     * Spring Data JPA générera automatiquement la requête SQL correspondante.
     */
    Optional<User> findByEmail(String email);
}
