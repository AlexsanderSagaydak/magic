package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.model.RegularUserProfile;
import com.magic_fans.wizards.model.WizardProfile;
import com.magic_fans.wizards.model.Subscription;
import com.magic_fans.wizards.service.UserService;
import com.magic_fans.wizards.service.WizardSkillsService;
import com.magic_fans.wizards.service.ProfileViewService;
import com.magic_fans.wizards.service.PostService;
import com.magic_fans.wizards.service.PostLikeService;
import com.magic_fans.wizards.repository.SubscriptionRepository;
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

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private WizardSkillsService wizardSkillsService;

    @Autowired
    private ProfileViewService profileViewService;

    @Autowired
    private PostService postService;

    @Autowired
    private PostLikeService postLikeService;

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

            // Set empty specialization for all users (will be set in profile)
            user.setSpecialization("");

            // Create role-specific profiles
            if ("regular".equals(user.getRole())) {
                RegularUserProfile regularProfile = new RegularUserProfile(user);
                user.setRegularProfile(regularProfile);
            } else if ("wizard".equals(user.getRole())) {
                WizardProfile wizardProfile = new WizardProfile(user);
                user.setWizardProfile(wizardProfile);
            }

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

            // Get user role to show conditional steps
            var user = userService.getUserById(id);
            if (user.isPresent()) {
                model.addAttribute("userRole", user.get().getRole());
            }
        }
        return "success";
    }

    @GetMapping("/{id}")
    public String getUserProfile(@PathVariable int id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser"))
            ? auth.getName()
            : null;

        return userService.getUserById(id)
                .map(viewedUser -> {
                    // If no one logged in or trying to view own profile
                    if (currentUsername == null) {
                        return "redirect:/login";
                    }

                    var currentUserOpt = userService.getUserByUsername(currentUsername);
                    if (currentUserOpt.isEmpty()) {
                        return "redirect:/login";
                    }

                    User currentUser = currentUserOpt.get();

                    // User can always view their own profile
                    if (currentUser.getId() == id) {
                        model.addAttribute("user", viewedUser);
                        model.addAttribute("isOwnProfile", true);

                        // Add skills for wizards
                        if ("wizard".equals(viewedUser.getRole()) && viewedUser.getWizardProfile() != null) {
                            model.addAttribute("userSkills",
                                wizardSkillsService.getAllSkillsForWizard(viewedUser.getWizardProfile().getId()));
                        }

                        // Add posts for wizards
                        if ("wizard".equals(viewedUser.getRole())) {
                            model.addAttribute("posts", postService.getPostsByAuthor(viewedUser.getId()));
                        }

                        return "my-profile";
                    }

                    // Regular users cannot view other regular users
                    if ("regular".equals(currentUser.getRole()) && "regular".equals(viewedUser.getRole())) {
                        return "redirect:/feed";
                    }

                    // Wizards cannot view other wizards
                    if ("wizard".equals(currentUser.getRole()) && "wizard".equals(viewedUser.getRole())) {
                        return "redirect:/feed";
                    }

                    // Regular user trying to view wizard - allow view, check subscription for later
                    boolean isSubscribed = false;
                    if ("regular".equals(currentUser.getRole()) && "wizard".equals(viewedUser.getRole())) {
                        isSubscribed = subscriptionRepository.existsByRegularUserIdAndWizardId(currentUser.getId(), viewedUser.getId());

                        // Record profile view
                        profileViewService.recordView(currentUser, viewedUser);
                    }

                    // Wizard viewing regular is allowed (but we don't show this in practice)
                    model.addAttribute("user", viewedUser);
                    model.addAttribute("isOwnProfile", false);
                    model.addAttribute("isSubscribed", isSubscribed);

                    // Add posts for wizards
                    if ("wizard".equals(viewedUser.getRole())) {
                        model.addAttribute("posts", postService.getPostsByAuthor(viewedUser.getId()));

                        // Check which posts current user has liked
                        var posts = postService.getPostsByAuthor(viewedUser.getId());
                        Map<Long, Boolean> likedPosts = new HashMap<>();
                        for (var post : posts) {
                            likedPosts.put(post.getId(), postLikeService.isLiked(post.getId(), currentUser.getId()));
                        }
                        model.addAttribute("likedPosts", likedPosts);
                    }

                    return "profile";
                })
                .orElse("redirect:/feed");
    }

    @PostMapping("/profile/save")
    public String saveProfileDetails(@RequestParam(required = false) String firstName,
                                    @RequestParam(required = false) String lastName,
                                    @RequestParam(required = false) String specialization,
                                    @RequestParam(required = false) String aboutMe,
                                    @RequestParam(required = false) String birthDate,
                                    @RequestParam(required = false) String birthPlace,
                                    @RequestParam(required = false) String birthTime,
                                    @RequestParam(required = false) Integer yearsOfExperience,
                                    @RequestParam(required = false, name = "skills") String[] skills,
                                    RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        try {
            // Get current authenticated user
            String username = auth.getName();
            var userOpt = userService.getUserByUsername(username);

            if (userOpt.isEmpty()) {
                return "redirect:/login";
            }

            User user = userOpt.get();

            // Update basic info
            if (firstName != null && !firstName.isEmpty()) {
                user.setFirstName(firstName);
            }
            if (lastName != null && !lastName.isEmpty()) {
                user.setLastName(lastName);
            }

            // Update specialization (required field)
            if (specialization != null && !specialization.isEmpty()) {
                user.setSpecialization(specialization);
            }

            // Update about me
            if (aboutMe != null) {
                user.setAboutMe(aboutMe.length() > 200 ? aboutMe.substring(0, 200) : aboutMe);
            }

            // Update role-specific profiles
            if ("regular".equals(user.getRole())) {
                RegularUserProfile profile = user.getRegularProfile();
                if (profile == null) {
                    profile = new RegularUserProfile(user);
                    user.setRegularProfile(profile);
                }

                if (birthDate != null && !birthDate.isEmpty()) {
                    profile.setBirthDate(java.time.LocalDate.parse(birthDate));
                }
                if (birthPlace != null && !birthPlace.isEmpty()) {
                    profile.setBirthPlace(birthPlace);
                }
                if (birthTime != null && !birthTime.isEmpty()) {
                    profile.setBirthTime(java.time.LocalTime.parse(birthTime));
                }
            } else if ("wizard".equals(user.getRole())) {
                WizardProfile profile = user.getWizardProfile();
                if (profile == null) {
                    profile = new WizardProfile(user);
                    user.setWizardProfile(profile);
                }

                if (yearsOfExperience != null) {
                    profile.setYearsOfExperience(yearsOfExperience);
                }

                // Update skills
                profile.getSkills().clear();
                if (skills != null) {
                    for (String skill : skills) {
                        if (skill != null && !skill.isEmpty()) {
                            profile.addSkill(skill);
                        }
                    }
                }
            }

            // Save updated user
            userService.saveUser(user);

            redirectAttributes.addAttribute("id", user.getId());
            return "redirect:/my-profile";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("error", "An error occurred while saving profile");
            return "redirect:/my-profile";
        }
    }

    @PostMapping("/subscribe/{wizardId}")
    public String subscribeToWizard(@PathVariable int wizardId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        try {
            String username = auth.getName();
            var currentUserOpt = userService.getUserByUsername(username);
            var wizardOpt = userService.getUserById(wizardId);

            if (currentUserOpt.isEmpty() || wizardOpt.isEmpty()) {
                return "redirect:/feed";
            }

            User currentUser = currentUserOpt.get();
            User wizard = wizardOpt.get();

            // Only regular users can subscribe to wizards
            if (!"regular".equals(currentUser.getRole()) || !"wizard".equals(wizard.getRole())) {
                return "redirect:/feed";
            }

            // Check if already subscribed
            if (subscriptionRepository.existsByRegularUserIdAndWizardId(currentUser.getId(), wizardId)) {
                redirectAttributes.addAttribute("error", "Already subscribed");
                return "redirect:/users/" + wizardId;
            }

            // Create subscription
            Subscription subscription = new Subscription(currentUser, wizard);
            subscriptionRepository.save(subscription);

            redirectAttributes.addAttribute("success", "Subscribed successfully");
            return "redirect:/users/" + wizardId;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("error", "An error occurred");
            return "redirect:/feed";
        }
    }

    @PostMapping("/unsubscribe/{wizardId}")
    public String unsubscribeFromWizard(@PathVariable int wizardId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        try {
            String username = auth.getName();
            var currentUserOpt = userService.getUserByUsername(username);

            if (currentUserOpt.isEmpty()) {
                return "redirect:/login";
            }

            User currentUser = currentUserOpt.get();

            var subscriptionOpt = subscriptionRepository.findByRegularUserIdAndWizardId(currentUser.getId(), wizardId);
            if (subscriptionOpt.isPresent()) {
                subscriptionRepository.delete(subscriptionOpt.get());
                redirectAttributes.addAttribute("success", "Unsubscribed successfully");
            }

            return "redirect:/users/" + wizardId;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("error", "An error occurred");
            return "redirect:/feed";
        }
    }

    /**
     * API endpoint to update user specialization
     */
    @PostMapping("/{userId}/specialization")
    @ResponseBody
    public Map<String, Object> updateSpecialization(@PathVariable Integer userId, @RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            String specialization = payload.get("specialization");

            if (specialization == null || specialization.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Specialization cannot be empty");
                return response;
            }

            var userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            User user = userOpt.get();
            user.setSpecialization(specialization.trim());
            userService.updateUser(user);

            response.put("success", true);
            response.put("message", "Specialization updated successfully");
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error updating specialization: " + e.getMessage());
            return response;
        }
    }
}