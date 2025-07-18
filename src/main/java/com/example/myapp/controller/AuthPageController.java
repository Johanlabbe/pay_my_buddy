package com.example.myapp.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String loginForm(@RequestParam(required = false) String error,
            Model model) {
        model.addAttribute("error", error);
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model m) {
        m.addAttribute("user", new CreateUserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") @Valid CreateUserDTO dto,
            RedirectAttributes ra) {
        UserDTO created = userService.create(dto);
        ra.addFlashAttribute("msg",
                "Inscription réussie pour " + created.getUsername() + ". Connectez-vous.");
        return "redirect:/auth/login";
    }

    // @PostMapping("/login")
    // public String login(@RequestParam String email,
    //         @RequestParam String password,
    //         RedirectAttributes ra) {

    //     String header = "Basic " + Base64.getEncoder()
    //             .encodeToString((email + ":" + password)
    //                     .getBytes(StandardCharsets.UTF_8));

    //     try {
    //         UserDTO me = api.get()
    //                 .uri("/users/profile")
    //                 .header(HttpHeaders.AUTHORIZATION, header)
    //                 .retrieve()
    //                 .bodyToMono(UserDTO.class)
    //                 .block();

    //         session.setAuthHeader(header);
    //         session.setUser(me);
    //         return "redirect:/dashboard";

    //     } catch (org.springframework.web.reactive.function.client.WebClientResponseException ex) {
    //         ra.addFlashAttribute("error", "Identifiants invalides");
    //         return "redirect:/auth/login";
    //     }
    // }

    @PostMapping("/logout")
    public String logout() {
        session.clear();
        return "redirect:/auth/login";
    }
}
