package com.example.myapp.service;

import com.example.myapp.dto.CreateTransactionDTO;
import com.example.myapp.dto.TransactionDTO;
import com.example.myapp.entity.Transaction;
import com.example.myapp.entity.User;
import com.example.myapp.repository.TransactionRepository;
import com.example.myapp.repository.UserConnectionRepository;
import com.example.myapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock private TransactionRepository txRepo;
    @Mock private UserRepository userRepo;
    @Mock private UserConnectionRepository connRepo;
    @InjectMocks private TransactionServiceImpl service;

    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        sender   = new User(); sender.setId(1L); sender.setBalance(new BigDecimal("100.00"));
        receiver = new User(); receiver.setId(2L); receiver.setBalance(new BigDecimal("50.00"));
    }

    @Test
    void findByUser_shouldThrow_whenUserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findByUser(1L));
        verify(userRepo).findById(1L);
        verifyNoMoreInteractions(txRepo, connRepo);
    }

    @Test
    void findByUser_shouldReturnSentAndReceived() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        Transaction t1 = new Transaction(); t1.setId(10L); t1.setSender(sender); t1.setReceiver(receiver);
        Transaction t2 = new Transaction(); t2.setId(11L); t2.setSender(receiver); t2.setReceiver(sender);
        when(txRepo.findBySender(sender)).thenReturn(List.of(t1));
        when(txRepo.findByReceiver(sender)).thenReturn(List.of(t2));

        List<TransactionDTO> result = service.findByUser(1L);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.getId().equals(10L)));
        assertTrue(result.stream().anyMatch(d -> d.getId().equals(11L)));
    }

    @Test
    void create_shouldThrow_whenSenderNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        CreateTransactionDTO dto = new CreateTransactionDTO(); dto.setReceiverId(2L); dto.setAmount(new BigDecimal("10"));

        assertThrows(EntityNotFoundException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_shouldThrow_whenNotConnected() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(connRepo.existsByUserAndConnection(sender, receiver)).thenReturn(false);

        CreateTransactionDTO dto = new CreateTransactionDTO(); dto.setReceiverId(2L); dto.setAmount(new BigDecimal("10"));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.create(1L, dto));
        assertEquals("Aucune connexion entre les utilisateurs", ex.getMessage());
    }

    @Test
    void create_shouldThrow_whenInsufficientBalance() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(connRepo.existsByUserAndConnection(sender, receiver)).thenReturn(true);

        CreateTransactionDTO dto = new CreateTransactionDTO(); dto.setReceiverId(2L); dto.setAmount(new BigDecimal("150"));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.create(1L, dto));
        assertEquals("Solde insuffisant", ex.getMessage());
    }

    @Test
    void create_shouldPersistTransaction_andUpdateBalances() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(connRepo.existsByUserAndConnection(sender, receiver)).thenReturn(true);

        CreateTransactionDTO dto = new CreateTransactionDTO();
        dto.setReceiverId(2L);
        dto.setAmount(new BigDecimal("20.00"));
        dto.setComment("Pour toi");

        Transaction savedTx = new Transaction();
        savedTx.setId(100L);
        savedTx.setSender(sender);
        savedTx.setReceiver(receiver);
        savedTx.setAmount(dto.getAmount());
        savedTx.setComment(dto.getComment());
        savedTx.setTimestamp(LocalDateTime.now());
        when(txRepo.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionDTO result = service.create(1L, dto);
        assertEquals(100L, result.getId());
        assertEquals(new BigDecimal("80.00"), sender.getBalance());
        assertEquals(new BigDecimal("70.00"), receiver.getBalance());
        verify(txRepo).save(any(Transaction.class));
        verify(userRepo).save(sender);
        verify(userRepo).save(receiver);
    }
}
