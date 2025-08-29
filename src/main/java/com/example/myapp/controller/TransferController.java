package com.example.myapp.controller;

import com.example.myapp.dto.CreateTransactionDTO;
import com.example.myapp.entity.User;
import com.example.myapp.repository.UserRepository;
import com.example.myapp.service.ConnectionService;
import com.example.myapp.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class TransferController {

    private final UserRepository userRepository;
    private final ConnectionService connectionService;
    private final TransactionService transactionService;

    public TransferController(UserRepository userRepository,
            ConnectionService connectionService,
            TransactionService transactionService) {
        this.userRepository = userRepository;
        this.connectionService = connectionService;
        this.transactionService = transactionService;
    }

    /* -------- Afficher le formulaire -------- */

    @GetMapping("/transfer")
    public String showTransferForm(Model model, Principal principal) {
        User current = userRepository.findByEmail(principal.getName())
                .orElseThrow();

        /* Liste d’amis (User) à afficher dans le <select>) */
        List<User> friends = connectionService.getConnections(current.getId())
                .stream()
                .map(conn -> userRepository.findById(conn.getFriendId()).orElse(null)) // <-- friendId
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        model.addAttribute("friends", friends);
        model.addAttribute("currentBalance", current.getBalance());

        if (!model.containsAttribute("transactionForm")) {
            model.addAttribute("transactionForm", new CreateTransactionDTO());
        }

        return "transfer";
    }

    /* -------- Traiter la soumission -------- */

    @PostMapping("/transfer")
    public String makeTransfer(@Valid @ModelAttribute("transactionForm") CreateTransactionDTO form,
            BindingResult binding,
            Principal principal,
            RedirectAttributes redirect) {

        if (binding.hasErrors()) {
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.transactionForm", binding);
            redirect.addFlashAttribute("transactionForm", form);
            return "redirect:/transfer";
        }

        User current = userRepository.findByEmail(principal.getName())
                .orElseThrow();

        try {
            transactionService.create(current.getId(), form);
            redirect.addFlashAttribute("success", "Transfert effectué !");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addFlashAttribute("transactionForm", form);
        }

        return "redirect:/transfer";
    }
}
