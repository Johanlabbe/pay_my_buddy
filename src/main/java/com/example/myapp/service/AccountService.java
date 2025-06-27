package com.example.myapp.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.myapp.entity.User;
import com.example.myapp.repository.UserRepository;

@Service
public class AccountService {

    private final UserRepository userRepo;

    public AccountService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public void checkSufficientBalance(User user, BigDecimal amount) {
        if (user.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Solde insuffisant");
        }
    }

    @Transactional
    public void debit(User user, BigDecimal amount) {
        user.setBalance(user.getBalance().subtract(amount));
        userRepo.save(user);
    }

    @Transactional
    public void credit(User user, BigDecimal amount) {
        user.setBalance(user.getBalance().add(amount));
        userRepo.save(user);
    }
}
