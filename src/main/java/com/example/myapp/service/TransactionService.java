package com.example.myapp.service;

import java.util.List;

import com.example.myapp.dto.CreateTransactionDTO;
import com.example.myapp.dto.TransactionDTO;
import com.example.myapp.entity.Transaction;

public interface TransactionService {
    List<TransactionDTO> findByUser(Long userId);
    TransactionDTO create(Long senderId, CreateTransactionDTO dto);
    List<Transaction> listForUser(Long userId);
}
