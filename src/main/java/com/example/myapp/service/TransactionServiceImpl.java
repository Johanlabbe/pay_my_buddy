package com.example.myapp.service;

import com.example.myapp.dto.CreateTransactionDTO;
import com.example.myapp.dto.TransactionDTO;
import com.example.myapp.entity.Transaction;
import com.example.myapp.entity.User;
import com.example.myapp.repository.TransactionRepository;
import com.example.myapp.repository.UserConnectionRepository;
import com.example.myapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;
    private final UserConnectionRepository connectionRepo;

    public TransactionServiceImpl(TransactionRepository transactionRepo,
                                  UserRepository userRepo,
                                  UserConnectionRepository connectionRepo) {
        this.transactionRepo = transactionRepo;
        this.userRepo = userRepo;
        this.connectionRepo = connectionRepo;
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransactionDTO> findByUser(Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé : " + userId));

        List<Transaction> sent     = transactionRepo.findBySender(user);
        List<Transaction> received = transactionRepo.findByReceiver(user);

        return Stream.concat(sent.stream(), received.stream())
                     .map(this::toDto)
                     .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TransactionDTO create(Long senderId, CreateTransactionDTO dto) {
        User sender = userRepo.findById(senderId)
            .orElseThrow(() -> new EntityNotFoundException("Expéditeur non trouvé : " + senderId));
        User receiver = userRepo.findById(dto.getReceiverId())
            .orElseThrow(() -> new EntityNotFoundException("Destinataire non trouvé : " + dto.getReceiverId()));

        boolean connected = connectionRepo.existsByUserAndConnection(sender, receiver);
        if (!connected) {
            throw new IllegalStateException("Aucune connexion entre les utilisateurs");
        }

        BigDecimal amount = dto.getAmount();
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Solde insuffisant");
        }

        Transaction tx = new Transaction();
        tx.setSender(sender);
        tx.setReceiver(receiver);
        tx.setAmount(amount);
        tx.setComment(dto.getComment());
        tx.setTimestamp(LocalDateTime.now());
        Transaction saved = transactionRepo.save(tx);

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        userRepo.save(sender);
        userRepo.save(receiver);

        return toDto(saved);
    }

    private TransactionDTO toDto(Transaction t) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(t.getId());
        dto.setSenderId(t.getSender().getId());
        dto.setReceiverId(t.getReceiver().getId());
        dto.setAmount(t.getAmount());
        dto.setComment(t.getComment());
        dto.setTimestamp(t.getTimestamp());
        return dto;
    }
}
