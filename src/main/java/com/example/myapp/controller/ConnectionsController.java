package com.example.myapp.controller;

import com.example.myapp.dto.AddConnectionForm;
import com.example.myapp.dto.ConnectionDTO;
import com.example.myapp.dto.UserDTO;
import com.example.myapp.entity.User;
import com.example.myapp.service.ConnectionService;
import com.example.myapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/connections")
@Validated
public class ConnectionsController {

    private final ConnectionService connectionService;
    private final UserService userService;

    public ConnectionsController(ConnectionService connectionService, UserService userService) {
        this.connectionService = connectionService;
        this.userService = userService;
    }

    /* -------- Afficher la page Contacts -------- */
    @GetMapping
    public String getConnections(Model model, Authentication authentication) {
        User current = userService.getCurrentUser(authentication);

        List<ConnectionDTO> links = connectionService.getConnections(current.getId());
        List<UserDTO> friends = links.stream()
                .map(link -> {
                    try {
                        return userService.findById(link.getFriendId());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        model.addAttribute("connections", friends);

        if (!model.containsAttribute("connectionForm")) {
            model.addAttribute("connectionForm", new AddConnectionForm());
        }

        return "connections";
    }

    /* -------- Ajouter un contact (POST /connections) -------- */
    @PostMapping
    public String addConnection(@Valid @ModelAttribute("connectionForm") AddConnectionForm form,
                                BindingResult binding,
                                Authentication authentication,
                                RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.connectionForm", binding);
            redirect.addFlashAttribute("connectionForm", form);
            return "redirect:/connections";
        }

        User current = userService.getCurrentUser(authentication);
        try {
            Long friendId = userService.findByEmail(form.getEmail()).getId();
            connectionService.add(new ConnectionDTO(current.getId(), friendId));
            redirect.addFlashAttribute("message", "Contact ajouté.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addFlashAttribute("connectionForm", form);
        }
        return "redirect:/connections";
    }

    /* -------- Suppression d'un contact -------- */
    @PostMapping("/remove")
    public String removeConnection(@RequestParam("friendId") Long friendId,
                                   Authentication authentication,
                                   RedirectAttributes redirect) {
        User current = userService.getCurrentUser(authentication);
        try {
            connectionService.remove(current.getId(), friendId);
            redirect.addFlashAttribute("message", "Contact supprimé.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/connections";
    }
}
