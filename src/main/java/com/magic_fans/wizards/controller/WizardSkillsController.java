package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.model.WizardProfile;
import com.magic_fans.wizards.service.UserService;
import com.magic_fans.wizards.service.WizardSkillsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for managing wizard skills.
 * Provides endpoints for saving and loading wizard skills in accordion format.
 *
 * @author Magic Fans Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/wizard/skills")
public class WizardSkillsController {

    private static final Logger logger = LoggerFactory.getLogger(WizardSkillsController.class);

    @Autowired
    private WizardSkillsService wizardSkillsService;

    @Autowired
    private UserService userService;

    /**
     * Get skills for the current authenticated wizard.
     *
     * @return ResponseEntity with skills data in structured format
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSkills() {
        // Validate authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            logger.warn("Unauthenticated user attempted to access wizard skills");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<>());
        }

        try {
            String username = auth.getName();
            Optional<User> currentUserOpt = userService.getUserByUsername(username);

            if (currentUserOpt.isEmpty()) {
                logger.error("User not found in database: {}", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>());
            }

            User currentUser = currentUserOpt.get();

            // Check if user is a wizard
            if (!"wizard".equalsIgnoreCase(currentUser.getRole())) {
                logger.warn("Non-wizard user {} attempted to access wizard skills", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new HashMap<>());
            }

            WizardProfile wizardProfile = currentUser.getWizardProfile();
            if (wizardProfile == null) {
                logger.error("Wizard profile not found for user: {}", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>());
            }

            // Get skills
            Map<String, Object> skills = wizardSkillsService.getSkillsForWizard(wizardProfile.getId());

            logger.info("User {} retrieved their wizard skills", username);

            Map<String, Object> response = new HashMap<>();
            response.put("skills", skills);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving wizard skills: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>());
        }
    }

    /**
     * Save skills for the current authenticated wizard.
     *
     * @param payload Map containing skills data
     * @return ResponseEntity with success status
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveSkills(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        // Validate authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            logger.warn("Unauthenticated user attempted to save wizard skills");
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String username = auth.getName();
            Optional<User> currentUserOpt = userService.getUserByUsername(username);

            if (currentUserOpt.isEmpty()) {
                logger.error("User not found in database: {}", username);
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User currentUser = currentUserOpt.get();

            // Check if user is a wizard
            if (!"wizard".equalsIgnoreCase(currentUser.getRole())) {
                logger.warn("Non-wizard user {} attempted to save wizard skills", username);
                response.put("success", false);
                response.put("message", "Only wizards can save skills");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            WizardProfile wizardProfile = currentUser.getWizardProfile();
            if (wizardProfile == null) {
                logger.error("Wizard profile not found for user: {}", username);
                response.put("success", false);
                response.put("message", "Wizard profile not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Extract skills from payload
            if (!payload.containsKey("skills")) {
                logger.warn("Invalid payload - missing 'skills' field");
                response.put("success", false);
                response.put("message", "Invalid payload");
                return ResponseEntity.badRequest().body(response);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> skillsData = (Map<String, Object>) payload.get("skills");

            // Save skills
            wizardSkillsService.saveSkillsForWizard(wizardProfile.getId(), skillsData);

            logger.info("User {} saved their wizard skills successfully", username);

            response.put("success", true);
            response.put("message", "Skills saved successfully");
            return ResponseEntity.ok(response);

        } catch (ClassCastException e) {
            logger.error("Invalid payload format: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Invalid data format");
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Error saving wizard skills: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get skills count for the current wizard.
     *
     * @return ResponseEntity with skills count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getSkillsCount() {
        Map<String, Long> response = new HashMap<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            response.put("count", 0L);
            return ResponseEntity.ok(response);
        }

        try {
            String username = auth.getName();
            Optional<User> currentUserOpt = userService.getUserByUsername(username);

            if (currentUserOpt.isEmpty() || currentUserOpt.get().getWizardProfile() == null) {
                response.put("count", 0L);
                return ResponseEntity.ok(response);
            }

            WizardProfile wizardProfile = currentUserOpt.get().getWizardProfile();
            long count = wizardSkillsService.getSkillCount(wizardProfile.getId());

            response.put("count", count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting skills count: {}", e.getMessage(), e);
            response.put("count", 0L);
            return ResponseEntity.ok(response);
        }
    }
}
