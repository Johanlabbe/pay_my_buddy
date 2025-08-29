package com.example.myapp.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.myapp.entity.User;
import com.example.myapp.service.UserService;


@Controller
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("balance", user.getBalance());
        return "dashboard";
    }
}
