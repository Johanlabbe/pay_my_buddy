package com.example.myapp.service;

import com.example.myapp.dto.CreateTransactionDTO;
import com.example.myapp.dto.TransactionDTO;
import com.example.myapp.entity.Transaction;
import com.example.myapp.entity.User;
import com.example.myapp.repository.TransactionRepository;
import com.example.myapp.repository.UserConnectionRepository;
import com.example.myapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

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
            List<Transaction> items =
                    transactionRepo.findBySenderOrReceiverOrderByTimestampDesc(user, user);
            log.debug("[TX] listByUser OK | userId={} | rows={}", userId, items.size());

            return items.stream()
                    .filter(Objects::nonNull)
                    .filter(t -> t.getSender() != null && t.getReceiver() != null && t.getAmount() != null)
                    .map(this::toDto)
                    .collect(Collectors.toList());

        } catch (RuntimeException e) {
            // Fallback si la méthode du repo n'est pas disponible/nommée différemment
            log.warn("[TX] listByUser fallback (repo combined query failed) | userId={} | cause={}",
                    userId, e.toString());

            List<Transaction> sent = transactionRepo.findBySender(user);
            List<Transaction> received = transactionRepo.findByReceiver(user);

            return Stream.concat(sent.stream(), received.stream())
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(
                                    Transaction::getTimestamp,
                                    Comparator.nullsLast(Comparator.naturalOrder()))
                            .reversed())
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    @Override
    public TransactionDTO create(Long senderId, CreateTransactionDTO dto) {
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Expéditeur non trouvé : " + senderId));
        User receiver = userRepo.findById(dto.getReceiverId())
                .orElseThrow(() -> new EntityNotFoundException("Destinataire non trouvé : " + dto.getReceiverId()));

        // Vérifie la connexion
        if (!connectionRepo.existsByUserAndConnection(sender, receiver)) {
            throw new IllegalStateException("Aucune connexion entre les utilisateurs");
        }

        // Vérifs d'entrée
        BigDecimal amount = dto.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Montant invalide");
        }

        // Solde par défaut à ZERO si null
        BigDecimal senderBalance = sender.getBalance() != null ? sender.getBalance() : BigDecimal.ZERO;
        BigDecimal receiverBalance = receiver.getBalance() != null ? receiver.getBalance() : BigDecimal.ZERO;

        if (senderBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Solde insuffisant");
        }

        // Enregistre la transaction
        Transaction tx = new Transaction();
        tx.setSender(sender);
        tx.setReceiver(receiver);
        tx.setAmount(amount);
        tx.setDescription(dto.getDescription());
        tx.setTimestamp(LocalDateTime.now());
        Transaction saved = transactionRepo.save(tx);

        // Met à jour les soldes
        sender.setBalance(senderBalance.subtract(amount));
        receiver.setBalance(receiverBalance.add(amount));
        userRepo.save(sender);
        userRepo.save(receiver);

        log.debug("[TX] create OK | senderId={} -> receiverId={} | amount={}", senderId, dto.getReceiverId(), amount);

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
