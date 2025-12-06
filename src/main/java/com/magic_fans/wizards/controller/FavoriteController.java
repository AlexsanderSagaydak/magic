package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.dto.FavoriteDTO;
import com.magic_fans.wizards.model.Favorite;
import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.repository.FavoriteRepository;
import com.magic_fans.wizards.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing user favorites (wizard bookmarks).
 * Provides REST API endpoints for adding, removing, and checking favorites.
 *
 * @author Magic Fans Team
 * @version 2.0
 */
@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserService userService;

    /**
     * Displays the favorites page with all user's favorited wizards.
     *
     * @param model the Spring MVC model
     * @return the favorites view template name
     */
    @GetMapping
    public String getFavorites(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            logger.warn("Unauthenticated user attempted to access favorites page");
            return "redirect:/login";
        }

        String username = auth.getName();
        Optional<User> currentUserOpt = userService.getUserByUsername(username);

        if (currentUserOpt.isEmpty()) {
            logger.error("User not found in database: {}", username);
            return "redirect:/login";
        }

        User currentUser = currentUserOpt.get();
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByAddedAtDesc(currentUser.getId());

        // Convert to DTOs with computed image URLs
        List<FavoriteDTO> favoriteDTOs = favorites.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        logger.info("User {} accessed favorites page with {} favorites", username, favorites.size());

        model.addAttribute("favorites", favoriteDTOs);
        model.addAttribute("favoriteCount", favoriteDTOs.size());

        return "favorites";
    }

    /**
     * Converts a Favorite entity to FavoriteDTO with computed image URLs.
     *
     * @param favorite the Favorite entity to convert
     * @return FavoriteDTO object with image URLs
     */
    private FavoriteDTO convertToDTO(Favorite favorite) {
        FavoriteDTO dto = new FavoriteDTO();
        User wizard = favorite.getFavoriteWizard();

        dto.setId(favorite.getId());
        dto.setWizardId(wizard.getId());
        dto.setUsername(wizard.getUsername());
        dto.setFirstName(wizard.getFirstName());
        dto.setLastName(wizard.getLastName());
        dto.setSpecialization(wizard.getSpecialization());
        dto.setAddedAt(favorite.getAddedAt());

        // Generate avatar URL using DiceBear API
        dto.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=" + wizard.getUsername());

        // Generate profile image URL using placeholder service
        String colorCode = String.format("%06X", wizard.getId() * 12345 & 0xFFFFFF);
        dto.setProfileImageUrl("https://via.placeholder.com/400x300/" + colorCode + "/FFFFFF?text=" + wizard.getUsername());

        return dto;
    }

    /**
     * Adds a wizard to the current user's favorites.
     *
     * @param wizardId the ID of the wizard to add to favorites
     * @return ResponseEntity with status and message
     */
    @PostMapping("/add/{wizardId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> addToFavorites(@PathVariable int wizardId) {
        Map<String, Object> response = new HashMap<>();

        // Validate authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            logger.warn("Unauthenticated user attempted to add wizard {} to favorites", wizardId);
            response.put("success", false);
            response.put("message", "Unauthorized");
            response.put("isFavorite", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String username = auth.getName();
            Optional<User> currentUserOpt = userService.getUserByUsername(username);
            Optional<User> wizardOpt = userService.getUserById(wizardId);

            // Validate users exist
            if (currentUserOpt.isEmpty()) {
                logger.error("Current user not found: {}", username);
                response.put("success", false);
                response.put("message", "User not found");
                response.put("isFavorite", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (wizardOpt.isEmpty()) {
                logger.error("Wizard not found with ID: {}", wizardId);
                response.put("success", false);
                response.put("message", "Wizard not found");
                response.put("isFavorite", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User currentUser = currentUserOpt.get();
            User wizard = wizardOpt.get();

            // Prevent users from favoriting themselves
            if (currentUser.getId() == wizardId) {
                logger.warn("User {} attempted to favorite themselves", username);
                response.put("success", false);
                response.put("message", "Cannot favorite yourself");
                response.put("isFavorite", false);
                return ResponseEntity.badRequest().body(response);
            }

            // Check if already in favorites
            if (favoriteRepository.existsByUserIdAndFavoriteWizardId(currentUser.getId(), wizardId)) {
                logger.debug("Wizard {} already in favorites for user {}", wizardId, username);
                response.put("success", true);
                response.put("message", "Already in favorites");
                response.put("isFavorite", true);
                return ResponseEntity.ok(response);
            }

            // Add to favorites
            Favorite favorite = new Favorite(currentUser, wizard);
            favoriteRepository.save(favorite);

            logger.info("User {} added wizard {} to favorites", username, wizardId);

            response.put("success", true);
            response.put("message", "Added to favorites");
            response.put("isFavorite", true);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error adding wizard {} to favorites: {}", wizardId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Internal server error");
            response.put("isFavorite", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Removes a wizard from the current user's favorites.
     *
     * @param wizardId the ID of the wizard to remove from favorites
     * @return ResponseEntity with status and message
     */
    @PostMapping("/remove/{wizardId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> removeFromFavorites(@PathVariable int wizardId) {
        Map<String, Object> response = new HashMap<>();

        // Validate authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            logger.warn("Unauthenticated user attempted to remove wizard {} from favorites", wizardId);
            response.put("success", false);
            response.put("message", "Unauthorized");
            response.put("isFavorite", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String username = auth.getName();
            Optional<User> currentUserOpt = userService.getUserByUsername(username);

            if (currentUserOpt.isEmpty()) {
                logger.error("Current user not found: {}", username);
                response.put("success", false);
                response.put("message", "User not found");
                response.put("isFavorite", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User currentUser = currentUserOpt.get();
            Optional<Favorite> favoriteOpt = favoriteRepository.findByUserIdAndFavoriteWizardId(
                    currentUser.getId(), wizardId);

            if (favoriteOpt.isPresent()) {
                favoriteRepository.delete(favoriteOpt.get());
                logger.info("User {} removed wizard {} from favorites", username, wizardId);

                response.put("success", true);
                response.put("message", "Removed from favorites");
                response.put("isFavorite", false);
                return ResponseEntity.ok(response);
            } else {
                logger.debug("Wizard {} not in favorites for user {}", wizardId, username);
                response.put("success", false);
                response.put("message", "Not in favorites");
                response.put("isFavorite", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            logger.error("Error removing wizard {} from favorites: {}", wizardId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Internal server error");
            response.put("isFavorite", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Checks if a wizard is in the current user's favorites.
     *
     * @param wizardId the ID of the wizard to check
     * @return ResponseEntity with favorite status
     */
    @GetMapping("/check/{wizardId}")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkFavorite(@PathVariable int wizardId) {
        Map<String, Boolean> response = new HashMap<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            response.put("isFavorite", false);
            return ResponseEntity.ok(response);
        }

        try {
            String username = auth.getName();
            Optional<User> currentUserOpt = userService.getUserByUsername(username);

            if (currentUserOpt.isEmpty()) {
                response.put("isFavorite", false);
                return ResponseEntity.ok(response);
            }

            User currentUser = currentUserOpt.get();
            boolean isFavorite = favoriteRepository.existsByUserIdAndFavoriteWizardId(
                    currentUser.getId(), wizardId);

            response.put("isFavorite", isFavorite);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error checking favorite status for wizard {}: {}", wizardId, e.getMessage(), e);
            response.put("isFavorite", false);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Gets the count of favorites for the current user.
     *
     * @return ResponseEntity with favorite count
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getFavoriteCount() {
        Map<String, Long> response = new HashMap<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            response.put("count", 0L);
            return ResponseEntity.ok(response);
        }

        try {
            String username = auth.getName();
            Optional<User> currentUserOpt = userService.getUserByUsername(username);

            if (currentUserOpt.isEmpty()) {
                response.put("count", 0L);
                return ResponseEntity.ok(response);
            }

            User currentUser = currentUserOpt.get();
            long count = favoriteRepository.countByUserId(currentUser.getId());

            response.put("count", count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting favorite count: {}", e.getMessage(), e);
            response.put("count", 0L);
            return ResponseEntity.ok(response);
        }
    }
}
