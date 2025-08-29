package com.example.myapp.controller;

import com.example.myapp.dto.CreateTransactionDTO;
import com.example.myapp.dto.TransactionDTO;
import com.example.myapp.dto.ConnectionDTO;
import com.example.myapp.entity.User;
import com.example.myapp.repository.UserRepository;
import com.example.myapp.service.ConnectionService;
import com.example.myapp.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);

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

    /* -------- Afficher formulaire + historique -------- */

    @GetMapping("/transfer")
    public String showTransferForm(Model model, Principal principal) {
        User current = userRepository.findByEmail(principal.getName())
                .orElseThrow();

        // Solde + formulaire
        model.addAttribute("currentBalance", current.getBalance());
        if (!model.containsAttribute("transactionForm")) {
            model.addAttribute("transactionForm", new CreateTransactionDTO());
        }

        // Liste d'amis (fallback sûr)
        try {
            List<User> friends = connectionService.getConnections(current.getId())
                    .stream()
                    .map((ConnectionDTO c) -> userRepository.findById(
                            // NOTE : si ton DTO expose connectionId au lieu de friendId, remplace ci-dessous
                            c.getFriendId()
                    ).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            model.addAttribute("friends", friends);
        } catch (Exception e) {
            log.error("Erreur au chargement des amis pour l'utilisateur {}", current.getId(), e);
            model.addAttribute("friends", Collections.emptyList());
            model.addAttribute("error", "Impossible d'afficher la liste d'amis pour le moment.");
        }

        // Historique (fallback sûr)
        try {
            List<TransactionDTO> history = transactionService.findByUser(current.getId());
            model.addAttribute("transactions", history);
        } catch (Exception e) {
            log.error("Erreur au chargement de l'historique pour l'utilisateur {}", current.getId(), e);
            model.addAttribute("transactions", Collections.emptyList());
            // On n’écrase pas un éventuel message existant
            if (model.getAttribute("error") == null) {
                model.addAttribute("error", "Impossible d'afficher l'historique pour le moment.");
            }
        }

        model.addAttribute("currentUserId", current.getId());
        return "transfer";
    }

    /* -------- Soumission -------- */

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

        User current = userRepository.findByEmail(principal.getName()).orElseThrow();

        try {
            transactionService.create(current.getId(), form);
            redirect.addFlashAttribute("success", "Transfert effectué !");
        } catch (IllegalStateException | EntityNotFoundException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addFlashAttribute("transactionForm", form);
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "Une erreur inattendue est survenue");
            redirect.addFlashAttribute("transactionForm", form);
        }

        return "redirect:/transfer";
    }
}
