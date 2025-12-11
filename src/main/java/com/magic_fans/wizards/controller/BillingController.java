package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.model.Favorite;
import com.magic_fans.wizards.model.PostLike;
import com.magic_fans.wizards.model.ProfileView;
import com.magic_fans.wizards.model.Subscription;
import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.model.WizardService;
import com.magic_fans.wizards.service.FavoriteService;
import com.magic_fans.wizards.service.PostLikeService;
import com.magic_fans.wizards.service.ProfileViewService;
import com.magic_fans.wizards.service.SubscriptionService;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/billing")
public class BillingController {

    @Autowired
    private UserService userService;

    @Autowired
    private WizardServiceService wizardServiceService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private ProfileViewService profileViewService;

    @Autowired
    private PostLikeService postLikeService;

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

            // Load statistics
            long favoritesCount = favoriteService.getWizardFavoritesCount(user.getId());
            long subscribersCount = subscriptionService.getWizardSubscribersCount(user.getId());
            long viewsCount = profileViewService.getWizardViewsCount(user.getId());
            long likesCount = postLikeService.getTotalLikesByAuthor(user.getId());
            model.addAttribute("favoritesCount", favoritesCount);
            model.addAttribute("subscribersCount", subscribersCount);
            model.addAttribute("viewsCount", viewsCount);
            model.addAttribute("likesCount", likesCount);

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

    @GetMapping("/statistics/favorites")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFavoritesStatistics() {
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

            // Get list of users who added this wizard to favorites
            List<Favorite> favorites = favoriteService.getWizardFavoredBy(user.getId());

            // Map to simple user data
            List<Map<String, Object>> users = favorites.stream()
                .map(fav -> {
                    Map<String, Object> userData = new HashMap<>();
                    User favUser = fav.getUser();
                    userData.put("id", favUser.getId());
                    userData.put("username", favUser.getUsername());
                    userData.put("firstName", favUser.getFirstName());
                    userData.put("lastName", favUser.getLastName());
                    userData.put("addedAt", fav.getAddedAt().toString());
                    return userData;
                })
                .collect(Collectors.toList());

            response.put("success", true);
            response.put("count", favorites.size());
            response.put("users", users);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/subscribers")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSubscribersStatistics() {
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

            // Get list of subscribers
            List<Subscription> subscriptions = subscriptionService.getWizardSubscribers(user.getId());

            // Map to simple user data
            List<Map<String, Object>> users = subscriptions.stream()
                .map(sub -> {
                    Map<String, Object> userData = new HashMap<>();
                    User subscriber = sub.getSubscriber();
                    userData.put("id", subscriber.getId());
                    userData.put("username", subscriber.getUsername());
                    userData.put("firstName", subscriber.getFirstName());
                    userData.put("lastName", subscriber.getLastName());
                    userData.put("subscribedAt", sub.getSubscribedAt().toString());
                    return userData;
                })
                .collect(Collectors.toList());

            response.put("success", true);
            response.put("count", subscriptions.size());
            response.put("users", users);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/views")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getViewsStatistics() {
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

            // Get list of viewers
            List<ProfileView> views = profileViewService.getWizardViewers(user.getId());

            // Map to simple user data
            List<Map<String, Object>> users = views.stream()
                .map(view -> {
                    Map<String, Object> userData = new HashMap<>();
                    User viewer = view.getViewer();
                    userData.put("id", viewer.getId());
                    userData.put("username", viewer.getUsername());
                    userData.put("firstName", viewer.getFirstName());
                    userData.put("lastName", viewer.getLastName());
                    userData.put("viewedAt", view.getViewedAt().toString());
                    return userData;
                })
                .collect(Collectors.toList());

            response.put("success", true);
            response.put("count", views.size());
            response.put("users", users);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/likes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLikesStatistics() {
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

            // Get all likes for this wizard's posts
            List<PostLike> likes = postLikeService.getAllLikesByAuthor(user.getId());

            // Map to simple user data
            List<Map<String, Object>> users = likes.stream()
                .map(like -> {
                    Map<String, Object> userData = new HashMap<>();
                    User likeUser = like.getUser();
                    userData.put("id", likeUser.getId());
                    userData.put("username", likeUser.getUsername());
                    userData.put("firstName", likeUser.getFirstName());
                    userData.put("lastName", likeUser.getLastName());
                    userData.put("likedAt", like.getLikedAt().toString());
                    userData.put("postId", like.getPost().getId());
                    return userData;
                })
                .collect(Collectors.toList());

            response.put("success", true);
            response.put("count", likes.size());
            response.put("users", users);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
