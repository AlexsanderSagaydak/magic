package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.model.WizardService;
import com.magic_fans.wizards.service.UserService;
import com.magic_fans.wizards.service.WizardServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/billing")
public class BillingController {

    @Autowired
    private UserService userService;

    @Autowired
    private WizardServiceService wizardServiceService;

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

        // Load services for wizard users
        if ("wizard".equals(user.getRole())) {
            List<WizardService> services = wizardServiceService.getServicesByUserId(user.getId());
            model.addAttribute("services", services);
            return "billing-wizard";
        } else {
            return "billing-regular";
        }
    }

    @PostMapping("/service/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveService(@RequestBody WizardService service) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                response.put("success", false);
                response.put("message", "Not authenticated");
                return ResponseEntity.ok(response);
            }

            String username = auth.getName();
            var userOpt = userService.getUserByUsername(username);

            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();
            service.setUserId(user.getId());

            wizardServiceService.saveService(service);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/service/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteService(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                response.put("success", false);
                response.put("message", "Not authenticated");
                return ResponseEntity.ok(response);
            }

            String username = auth.getName();
            var userOpt = userService.getUserByUsername(username);

            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();

            // Verify that the service belongs to the current user
            var serviceOpt = wizardServiceService.getServiceById(id);
            if (serviceOpt.isEmpty() || !serviceOpt.get().getUserId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "Service not found or access denied");
                return ResponseEntity.ok(response);
            }

            wizardServiceService.deleteService(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
