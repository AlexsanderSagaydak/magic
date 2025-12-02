package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.dto.UserProfileDTO;
import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API Controller for user profile feed (infinite scroll).
 * Provides endpoints for fetching user profiles for the feed/listing view.
 *
 * @author Magic Fans Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProfileFeedController {

    @Autowired
    private UserService userService;

    /**
     * Gets a paginated list of user profiles for the feed.
     * Supports offset-based pagination for infinite scroll.
     *
     * @param offset the starting offset for pagination (default 0)
     * @param limit the maximum number of profiles to return (default 10, max 50)
     * @return ResponseEntity containing list of UserProfileDTO objects
     */
    @GetMapping("")
    public ResponseEntity<List<UserProfileDTO>> getProfilesFeed(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        // Validate and limit the requested amount
        if (limit > 50) {
            limit = 50;
        }
        if (limit < 1) {
            limit = 10;
        }
        if (offset < 0) {
            offset = 0;
        }

        // Get all users and apply pagination
        List<UserProfileDTO> profiles = userService.getAllUsers()
                .stream()
                .filter(User::isActive)  // Only active users
                .skip(offset)             // Skip offset items
                .limit(limit)             // Limit to requested amount
                .map(this::convertToDTO)  // Convert to DTO
                .collect(Collectors.toList());

        return ResponseEntity.ok(profiles);
    }

    /**
     * Gets profiles by specialization.
     *
     * @param specialization the magical specialization to filter by
     * @param offset the starting offset for pagination (default 0)
     * @param limit the maximum number of profiles to return (default 10)
     * @return ResponseEntity containing list of UserProfileDTO objects
     */
    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<UserProfileDTO>> getProfilesBySpecialization(
            @PathVariable String specialization,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        if (limit > 50) {
            limit = 50;
        }
        if (limit < 1) {
            limit = 10;
        }
        if (offset < 0) {
            offset = 0;
        }

        List<UserProfileDTO> profiles = userService.getAllUsers()
                .stream()
                .filter(User::isActive)
                .filter(u -> u.getSpecialization().equalsIgnoreCase(specialization))
                .skip(offset)
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(profiles);
    }

    /**
     * Searches for profiles by username or name.
     *
     * @param query the search query (searches in username, firstName, lastName)
     * @param offset the starting offset for pagination (default 0)
     * @param limit the maximum number of profiles to return (default 10)
     * @return ResponseEntity containing list of UserProfileDTO objects
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileDTO>> searchProfiles(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (limit > 50) {
            limit = 50;
        }
        if (limit < 1) {
            limit = 10;
        }
        if (offset < 0) {
            offset = 0;
        }

        String searchQuery = query.toLowerCase();

        List<UserProfileDTO> profiles = userService.getAllUsers()
                .stream()
                .filter(User::isActive)
                .filter(u -> u.getUsername().toLowerCase().contains(searchQuery) ||
                           u.getFirstName().toLowerCase().contains(searchQuery) ||
                           u.getLastName().toLowerCase().contains(searchQuery))
                .skip(offset)
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(profiles);
    }

    /**
     * Converts User entity to UserProfileDTO.
     * Maps user data to DTO format for API response.
     *
     * @param user the User entity to convert
     * @return UserProfileDTO object
     */
    private UserProfileDTO convertToDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setSpecialization(user.getSpecialization());

        // Use DiceBear API for dynamic avatars based on username
        String seed = user.getUsername();
        dto.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=" + seed);

        // Use gradient placeholder for profile cover image (via placeholder service)
        String colorCode = String.format("%06X", user.getId() * 12345 & 0xFFFFFF);
        dto.setProfileImageUrl("https://via.placeholder.com/400x300/" + colorCode + "/FFFFFF?text=" + seed);
        dto.setVideoUrl("/videos/profile-" + user.getId() + ".mp4");

        // TODO: Implement actual online status checking (when websocket or last activity tracking is added)
        dto.setOnline(false);

        return dto;
    }

    /**
     * Gets total count of active users.
     *
     * @return ResponseEntity containing total count
     */
    @GetMapping("/count")
    public ResponseEntity<Integer> getTotalProfilesCount() {
        int count = (int) userService.getAllUsers()
                .stream()
                .filter(User::isActive)
                .count();
        return ResponseEntity.ok(count);
    }
}