package com.example.myapp.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.myapp.dto.CreateUserDTO;
import com.example.myapp.dto.UserDTO;
import com.example.myapp.service.UserService;
import com.example.myapp.session.AuthSession;

import jakarta.persistence.EntityExistsException;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthPageController {

    private final UserService userService;
    private final WebClient api;
    private final AuthSession session;

    public AuthPageController(UserService userService,
            WebClient api,
            AuthSession session) {
        this.userService = userService;
        this.api = api;
        this.session = session;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userForm", new CreateUserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(
            @Valid CreateUserDTO userForm,
            BindingResult br,
            RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "register";
        }
        try {
            userService.create(userForm);
            ra.addFlashAttribute("success", "Inscription réussie, veuillez vous connecter.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }
}
