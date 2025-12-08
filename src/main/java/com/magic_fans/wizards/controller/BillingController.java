package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/billing")
public class BillingController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public String billing(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String username = auth.getName();
        var userOpt = userService.getUserByUsername(username);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);

        // Redirect to role-specific billing page
        if ("wizard".equals(user.getRole())) {
            return "billing-wizard";
        } else {
            return "billing-regular";
        }
    }
}
