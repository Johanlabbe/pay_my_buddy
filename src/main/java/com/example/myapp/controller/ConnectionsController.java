package com.example.myapp.controller;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.myapp.dto.ConnectionDTO;
import com.example.myapp.entity.User;
import com.example.myapp.service.ConnectionService;
import com.example.myapp.service.UserService;

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

    @GetMapping
    public String getConnections(Model model, Authentication authentication,
                                 @RequestParam(value = "msg", required = false) String msg,
                                 @RequestParam(value = "err", required = false) String err) {

        User current = userService.getCurrentUser(authentication);

        // 🔁 utilise bien le contrat: List<ConnectionDTO>
        List<ConnectionDTO> connections = connectionService.getConnections(current.getId());

        model.addAttribute("connections", connections);
        model.addAttribute("addEmail", "");
        model.addAttribute("message", msg);
        model.addAttribute("error", err);
        return "connections/index";
    }

    @PostMapping("/add")
    public String addConnection(Authentication authentication,
                                @RequestParam("email")
                                @NotBlank @Email String email) {
        User current = userService.getCurrentUser(authentication);
        try {
            // Résoudre l'email en friendId
            Long friendId = userService.findByEmail(email).getId();

            // Appel conforme au service
            connectionService.add(new ConnectionDTO(current.getId(), friendId));

            return "redirect:/connections?msg=Contact%20ajout%C3%A9";
        } catch (IllegalArgumentException | jakarta.persistence.EntityNotFoundException ex) {
            return "redirect:/connections?err=" + urlEncode(ex.getMessage());
        } catch (Exception ex) {
            return "redirect:/connections?err=Unable%20to%20add%20contact";
        }
    }

    @PostMapping("/remove")
    public String removeConnection(Authentication authentication,
                                   @RequestParam("friendId") Long friendId) {
        User current = userService.getCurrentUser(authentication);
        try {
            // Appel conforme au service
            connectionService.remove(current.getId(), friendId);

            return "redirect:/connections?msg=Contact%20supprim%C3%A9";
        } catch (IllegalArgumentException | jakarta.persistence.EntityNotFoundException ex) {
            return "redirect:/connections?err=" + urlEncode(ex.getMessage());
        } catch (Exception ex) {
            return "redirect:/connections?err=Unable%20to%20remove%20contact";
        }
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "error";
        }
    }
}
