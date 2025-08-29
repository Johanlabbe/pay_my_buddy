package com.example.myapp.controller;

import com.example.myapp.dto.CreateTransactionDTO;
import com.example.myapp.entity.User;
import com.example.myapp.repository.TransactionRepository;
import com.example.myapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepo;
    @Autowired
    TransactionRepository txRepo;
    @Autowired
    ObjectMapper mapper;

    private User sender;
    private User receiver;

    @BeforeEach
    void setup() {
        txRepo.deleteAll();
        userRepo.deleteAll();

        sender = new User();
        sender.setEmail("alice@example.com");
        sender.setUsername("alice");
        sender.setPassword("passw0rd");
        sender.setRole("USER");
        sender.setBalance(new BigDecimal("100.00"));
        sender = userRepo.save(sender);

        receiver = new User();
        receiver.setEmail("bob@example.com");
        receiver.setUsername("bob");
        receiver.setPassword("passw0rd");
        receiver.setRole("USER");
        receiver.setBalance(new BigDecimal("50.00"));
        receiver = userRepo.save(receiver);

        // TODO connexion BDD
    }

    @Test
    void whenGetTransactions_thenEmptyList() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/transactions", sender.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void whenPostValidTransaction_thenCreatedAndBalancesUpdated() throws Exception {
        // TODO connexion BDD

        CreateTransactionDTO dto = new CreateTransactionDTO();
        dto.setReceiverId(receiver.getId());
        dto.setAmount(new BigDecimal("20.00"));
        dto.setDescription("Test IT");

        mockMvc.perform(post("/api/users/{userId}/transactions", sender.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.senderId").value(sender.getId()))
                .andExpect(jsonPath("$.receiverId").value(receiver.getId()))
                .andExpect(jsonPath("$.amount").value(20.00))
                .andExpect(jsonPath("$.description").value("Test IT"));

        User refreshedSender = userRepo.findById(sender.getId()).orElseThrow();
        User refreshedReceiver = userRepo.findById(receiver.getId()).orElseThrow();
        assert (refreshedSender.getBalance().compareTo(new BigDecimal("80.00")) == 0);
        assert (refreshedReceiver.getBalance().compareTo(new BigDecimal("70.00")) == 0);
    }

    @Test
    void whenPostWithoutConnection_thenForbidden() throws Exception {
        CreateTransactionDTO dto = new CreateTransactionDTO();
        dto.setReceiverId(receiver.getId());
        dto.setAmount(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/users/{userId}/transactions", sender.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(containsString("connexion")));

    }

    @Test
    void whenPostInsufficientBalance_thenForbidden() throws Exception {
        // TODO connexion BDD

        CreateTransactionDTO dto = new CreateTransactionDTO();
        dto.setReceiverId(receiver.getId());
        dto.setAmount(new BigDecimal("150.00"));

        mockMvc.perform(post("/api/users/{userId}/transactions", sender.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Solde insuffisant"));
    }
}
