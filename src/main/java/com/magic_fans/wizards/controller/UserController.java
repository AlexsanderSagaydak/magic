package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        // Validate username doesn't exist
        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", "Username already exists");
            model.addAttribute("user", user);
            return "register";
        }

        // Validate email doesn't exist
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("error", "Email already registered");
            model.addAttribute("user", user);
            return "register";
        }

        try {
            // Store original password for authentication
            String plainPassword = user.getPassword();

            // Ensure user is marked as active
            user.setActive(true);

            // Encode password before saving
            user.setPassword(passwordEncoder.encode(plainPassword));
            User savedUser = userService.saveUser(user);

            // Auto-login user after registration
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), plainPassword);
            Authentication auth = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Store authentication in session to persist across requests
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            redirectAttributes.addAttribute("id", savedUser.getId());
            redirectAttributes.addAttribute("name", savedUser.getFirstName());
            return "redirect:/users/success";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration. Please try again.");
            model.addAttribute("user", user);
            return "register";
        }
    }

    @GetMapping("/success")
    public String successPage(@RequestParam(required = false) Integer id,
                              @RequestParam(required = false) String name,
                              Model model) {
        if (id != null) {
            model.addAttribute("userId", id);
            model.addAttribute("userName", name != null ? name : "User");
        }
        return "success";
    }

    @GetMapping("/{id}")
    public String getUserProfile(@PathVariable int id, Model model) {
        return userService.getUserById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    return "user-profile";
                })
                .orElse("error");
    }
}