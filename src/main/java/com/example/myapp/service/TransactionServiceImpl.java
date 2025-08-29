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

        try {
            List<Transaction> items = transactionRepo.findBySenderOrReceiverOrderByTimestampDesc(user, user);
            org.slf4j.LoggerFactory.getLogger(getClass()).debug(
                    "[TX] listByUser OK via ORderByTimestampDesc | userId={} | rows={}", userId, items.size());
            return items.stream()
                    .filter(t -> t != null && t.getSender() != null && t.getReceiver() != null && t.getAmount() != null)
                    .map(this::toDto)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).error(
                    "[TX] listByUser FAILED via ORderByTimestampDesc | userId={} | cause={}",
                    userId, e.toString(), e);

            List<Transaction> sent = transactionRepo.findBySender(user);
            List<Transaction> received = transactionRepo.findByReceiver(user);
            List<Transaction> merged = java.util.stream.Stream.concat(sent.stream(), received.stream())
                    .sorted(java.util.Comparator.comparing(Transaction::getTimestamp,
                            java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())).reversed())
                    .toList();

            org.slf4j.LoggerFactory.getLogger(getClass()).debug(
                    "[TX] listByUser FALLBACK | sent={} received={} merged={}",
                    sent.size(), received.size(), merged.size());

            return merged.stream()
                    .filter(t -> t != null && t.getSender() != null && t.getReceiver() != null && t.getAmount() != null)
                    .map(this::toDto)
                    .collect(java.util.stream.Collectors.toList());
        }
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
        tx.setDescription(dto.getDescription());
        tx.setTimestamp(LocalDateTime.now());
        Transaction saved = transactionRepo.save(tx);

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        userRepo.save(sender);
        userRepo.save(receiver);

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> listForUser(Long userId) {
        User me = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : " + userId));
        return transactionRepo.findBySenderOrReceiverOrderByTimestampDesc(me, me);
    }

    private TransactionDTO toDto(Transaction t) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(t.getId());
        dto.setSenderId(t.getSender() != null ? t.getSender().getId() : null);
        dto.setReceiverId(t.getReceiver() != null ? t.getReceiver().getId() : null);
        dto.setAmount(t.getAmount());
        dto.setDescription(t.getDescription());
        dto.setTimestamp(t.getTimestamp());
        return dto;
    }
}
