package com.example.myapp.service;

import com.example.myapp.dto.CreateTransactionDTO;
import com.example.myapp.dto.TransactionDTO;

import java.util.List;

public interface TransactionService {
    List<TransactionDTO> findByUser(Long userId);
    TransactionDTO create(Long senderId, CreateTransactionDTO dto);
}
