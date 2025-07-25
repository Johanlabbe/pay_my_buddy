package com.example.myapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.myapp.entity.User;
import com.example.myapp.repository.UserRepository;

import java.security.Principal;

@Controller
public class DashboardController {

    private final UserRepository userRepository;

    public DashboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Affiche le tableau de bord principal après connexion.
     * URL : GET /dashboard  (voir redirection dans SecurityConfig)
     */
    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        // Récupère l'utilisateur courant grâce à son email (principal.getName())
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Utilisateur courant introuvable"));

        model.addAttribute("username", user.getUsername());
        model.addAttribute("balance", user.getBalance());

        return "dashboard";  // templates/dashboard.html
    }
}
