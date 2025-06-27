package com.example.myapp.controller;

import com.example.myapp.dto.CreateTransactionDTO;
import com.example.myapp.dto.TransactionDTO;
import com.example.myapp.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Liste toutes les transactions (envoyées + reçues) pour un utilisateur donné.
     */
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getTransactionsByUser(@PathVariable Long userId) {
        List<TransactionDTO> transactions = transactionService.findByUser(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Crée une nouvelle transaction pour l'utilisateur senderId.
     */
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(
            @PathVariable Long userId,
            @Valid @RequestBody CreateTransactionDTO request
    ) {
        TransactionDTO created = transactionService.create(userId, request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
