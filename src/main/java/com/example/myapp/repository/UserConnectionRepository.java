package com.example.myapp.repository;

import com.example.myapp.entity.User;
import com.example.myapp.entity.UserConnection;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {
    List<UserConnection> findByUser(User user);

    boolean existsByUserAndConnection(User user, User connection);

    Optional<UserConnection> findByUserAndConnection(User user, User connection);
}