package com.example.myapp.controller;

import com.example.myapp.dto.UpdateProfileDTO;
import com.example.myapp.dto.UserDTO;
import com.example.myapp.entity.User;
import com.example.myapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(Model model, Authentication authentication) {
        User current = userService.getCurrentUser(authentication);
        UserDTO userDto = UserDTO.fromEntity(current);

        if (!model.containsAttribute("profileForm")) {
            UpdateProfileDTO form = new UpdateProfileDTO();
            form.setEmail(userDto.getEmail());
            form.setUsername(userDto.getUsername());
            model.addAttribute("profileForm", form);
        }

        model.addAttribute("user", userDto);
        model.addAttribute("active", "profile"); // pour la navbar
        return "profile";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("profileForm") UpdateProfileDTO form,
                                BindingResult binding,
                                Authentication authentication,
                                RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.profileForm", binding);
            redirect.addFlashAttribute("profileForm", form);
            redirect.addFlashAttribute("error", "Veuillez corriger les erreurs du formulaire.");
            return "redirect:/profile";
        }

        User current = userService.getCurrentUser(authentication);
        try {
            userService.updateProfile(current.getId(), form);
            redirect.addFlashAttribute("success", "Profil mis à jour.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addFlashAttribute("profileForm", form);
        }

        return "redirect:/profile";
    }
}
