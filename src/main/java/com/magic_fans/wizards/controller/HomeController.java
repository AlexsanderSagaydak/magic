package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.service.UserService;
import com.magic_fans.wizards.service.WizardSkillsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private WizardSkillsService wizardSkillsService;

    @GetMapping("/")
    public String home(Model model, HttpServletResponse response) {
        // Disable caching for dynamic content
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/feed";
        }

        // Redirect to login if not authenticated
        return "redirect:/login";
    }

    @GetMapping("/feed")
    public String feed(Model model, HttpServletResponse response) {
        // Disable caching for dynamic content
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            model.addAttribute("username", auth.getName());
            return "feed";
        }

        // Redirect to login if not authenticated
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Authentication auth, HttpServletResponse response) {
        // Disable caching for dynamic content
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // If already authenticated, redirect to home
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/";
        }
        // Return login page template
        return "login";
    }

    @GetMapping("/my-profile")
    public String myProfile(Model model, HttpServletResponse response) {
        // Disable caching for dynamic content
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        // Get current user by username
        String username = auth.getName();
        var userOpt = userService.getUserByUsername(username);

        if (userOpt.isPresent()) {
            var user = userOpt.get();
            model.addAttribute("user", user);

            // Add skills for wizards
            if ("wizard".equals(user.getRole()) && user.getWizardProfile() != null) {
                model.addAttribute("userSkills",
                    wizardSkillsService.getAllSkillsForWizard(user.getWizardProfile().getId()));
            }

            return "my-profile";
        }

        // If user not found, redirect to feed
        return "redirect:/feed";
    }
}