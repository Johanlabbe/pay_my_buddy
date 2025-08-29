package com.example.myapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.myapp.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Recherche un utilisateur par son email.
     * @param email l'email unique de l'utilisateur
     * @return un Optional contenant l'utilisateur s'il existe, sinon vide
     */
    Optional<User> findByEmail(String email);
}
