package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.model.WizardProfile;
import com.magic_fans.wizards.model.RegularUserProfile;
import com.magic_fans.wizards.model.Subscription;
import com.magic_fans.wizards.repository.SubscriptionRepository;
import com.magic_fans.wizards.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.notNull;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles("test")
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private HttpSession session;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private User testRegularUser;
    private User testWizardUser;

    @BeforeEach
    void setUp() {
        testRegularUser = new User("testuser", "test@example.com", "encodedPassword123", "John", "Doe", "None");
        testRegularUser.setId(1);
        testRegularUser.setRole("regular");
        testRegularUser.setActive(true);
        testRegularUser.setRegularProfile(new RegularUserProfile(testRegularUser));

        testWizardUser = new User("merlin", "merlin@example.com", "encodedPassword456", "Merlin", "The Great", "White Magic");
        testWizardUser.setId(2);
        testWizardUser.setRole("wizard");
        testWizardUser.setActive(true);
        testWizardUser.setWizardProfile(new WizardProfile(testWizardUser));
    }

    @Test
    @DisplayName("Should show registration form")
    void testShowRegistrationForm() {
        // When
        String view = userController.showRegistrationForm(model);

        // Then
        assertEquals("register", view);
        verify(model, times(1)).addAttribute(eq("user"), any(User.class));
    }

    @Test
    @DisplayName("Should successfully register regular user")
    void testRegisterUserSuccessful() {
        // Given
        User newUser = new User("newuser", "new@example.com", "password123", "New", "User", "None");
        newUser.setRole("regular");

        when(userService.usernameExists("newuser")).thenReturn(false);
        when(userService.emailExists("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userService.saveUser(any(User.class))).thenReturn(newUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When
        String view = userController.registerUser(newUser, model, redirectAttributes, session);

        // Then
        assertEquals("redirect:/users/success", view);
        verify(userService, times(1)).saveUser(any(User.class));
        verify(redirectAttributes, times(1)).addAttribute("id", newUser.getId());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should prevent duplicate username registration")
    void testRegisterUserDuplicateUsername() {
        // Given
        User newUser = new User("testuser", "new@example.com", "password123", "New", "User", "None");
        newUser.setRole("regular");

        when(userService.usernameExists("testuser")).thenReturn(true);

        // When
        String view = userController.registerUser(newUser, model, redirectAttributes, session);

        // Then
        assertEquals("register", view);
        verify(model, times(1)).addAttribute("error", "Username already exists");
        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    @DisplayName("Should prevent duplicate email registration")
    void testRegisterUserDuplicateEmail() {
        // Given
        User newUser = new User("newuser", "test@example.com", "password123", "New", "User", "None");
        newUser.setRole("regular");

        when(userService.usernameExists("newuser")).thenReturn(false);
        when(userService.emailExists("test@example.com")).thenReturn(true);

        // When
        String view = userController.registerUser(newUser, model, redirectAttributes, session);

        // Then
        assertEquals("register", view);
        verify(model, times(1)).addAttribute("error", "Email already registered");
        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    @DisplayName("Should show success page with user info")
    void testSuccessPageWithUserInfo() {
        // Given
        when(userService.getUserById(1)).thenReturn(Optional.of(testRegularUser));

        // When
        String view = userController.successPage(1, "John", model);

        // Then
        assertEquals("success", view);
        verify(model, times(1)).addAttribute("userId", 1);
        verify(model, times(1)).addAttribute("userName", "John");
        verify(model, times(1)).addAttribute("userRole", "regular");
    }

    @Test
    @DisplayName("Should show success page without parameters")
    void testSuccessPageWithoutParameters() {
        // When
        String view = userController.successPage(null, null, model);

        // Then
        assertEquals("success", view);
        verify(userService, never()).getUserById(anyInt());
    }

    @Test
    @DisplayName("Should get user profile when user is logged in and viewing own profile")
    void testGetUserProfileOwnProfile() {
        // Given
        when(userService.getUserById(1)).thenReturn(Optional.of(testRegularUser));

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("testuser");
        when(auth.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testRegularUser));

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When
        String view = userController.getUserProfile(1, model);

        // Then
        assertEquals("my-profile", view);
        verify(model, times(1)).addAttribute("user", testRegularUser);
        verify(model, times(1)).addAttribute("isOwnProfile", true);
    }

    @Test
    @DisplayName("Should redirect to login when no one is authenticated")
    void testGetUserProfileNotAuthenticated() {
        // Given
        when(userService.getUserById(1)).thenReturn(Optional.of(testRegularUser));

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(auth.getPrincipal()).thenReturn("anonymousUser");

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When
        String view = userController.getUserProfile(1, model);

        // Then
        assertEquals("redirect:/login", view);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("Should prevent regular user from viewing other regular users")
    void testGetUserProfileRegularCannotViewRegular() {
        // Given
        User anotherRegularUser = new User("anotheruser", "another@example.com", "pwd", "Another", "User", "None");
        anotherRegularUser.setId(3);
        anotherRegularUser.setRole("regular");
        anotherRegularUser.setActive(true);

        when(userService.getUserById(3)).thenReturn(Optional.of(anotherRegularUser));

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("testuser");
        when(auth.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testRegularUser));

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When
        String view = userController.getUserProfile(3, model);

        // Then
        assertEquals("redirect:/feed", view);
    }

    @Test
    @DisplayName("Should prevent wizard from viewing other wizards")
    void testGetUserProfileWizardCannotViewWizard() {
        // Given
        User anotherWizardUser = new User("gandalf", "gandalf@example.com", "pwd", "Gandalf", "The Grey", "Gray Magic");
        anotherWizardUser.setId(3);
        anotherWizardUser.setRole("wizard");
        anotherWizardUser.setActive(true);
        anotherWizardUser.setWizardProfile(new WizardProfile(anotherWizardUser));

        when(userService.getUserById(3)).thenReturn(Optional.of(anotherWizardUser));

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("merlin");
        when(auth.getName()).thenReturn("merlin");
        when(userService.getUserByUsername("merlin")).thenReturn(Optional.of(testWizardUser));

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When
        String view = userController.getUserProfile(3, model);

        // Then
        assertEquals("redirect:/feed", view);
    }

    @Test
    @DisplayName("Should allow regular user to view wizard profile")
    void testGetUserProfileRegularCanViewWizard() {
        // Given
        when(userService.getUserById(2)).thenReturn(Optional.of(testWizardUser));
        when(subscriptionRepository.existsByRegularUserIdAndWizardId(1, 2)).thenReturn(false);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("testuser");
        when(auth.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testRegularUser));

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When
        String view = userController.getUserProfile(2, model);

        // Then
        assertEquals("profile", view);
        verify(model, times(1)).addAttribute("user", testWizardUser);
        verify(model, times(1)).addAttribute("isOwnProfile", false);
        verify(model, times(1)).addAttribute("isSubscribed", false);
    }

    @Test
    @DisplayName("Should mark as subscribed if regular user is already subscribed to wizard")
    void testGetUserProfileRegularSubscribedToWizard() {
        // Given
        when(userService.getUserById(2)).thenReturn(Optional.of(testWizardUser));
        when(subscriptionRepository.existsByRegularUserIdAndWizardId(1, 2)).thenReturn(true);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("testuser");
        when(auth.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testRegularUser));

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When
        String view = userController.getUserProfile(2, model);

        // Then
        assertEquals("profile", view);
        verify(model, times(1)).addAttribute("isSubscribed", true);
    }

    @Test
    @DisplayName("Should return not found when user doesn't exist")
    void testGetUserProfileNotFound() {
        // Given
        when(userService.getUserById(999)).thenReturn(Optional.empty());

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("testuser");
        when(auth.getName()).thenReturn("testuser");

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When
        String view = userController.getUserProfile(999, model);

        // Then
        assertEquals("redirect:/feed", view);
    }

    @Test
    @DisplayName("Should subscribe regular user to wizard successfully")
    void testSubscribeToWizardSuccess() {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testRegularUser));
        when(userService.getUserById(2)).thenReturn(Optional.of(testWizardUser));
        when(subscriptionRepository.existsByRegularUserIdAndWizardId(1, 2)).thenReturn(false);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("testuser");
        when(auth.getName()).thenReturn("testuser");

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When
        String view = userController.subscribeToWizard(2, redirectAttributes);

        // Then
        assertEquals("redirect:/users/2", view);
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should redirect to login when not authenticated for subscribe")
    void testSubscribeToWizardNotAuthenticated() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(auth.getPrincipal()).thenReturn("anonymousUser");

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When
        String view = userController.subscribeToWizard(2, redirectAttributes);

        // Then
        assertEquals("redirect:/login", view);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should prevent subscribing twice")
    void testSubscribeToWizardAlreadySubscribed() {
        // Given
        lenient().when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testRegularUser));
        lenient().when(userService.getUserById(2)).thenReturn(Optional.of(testWizardUser));
        lenient().when(subscriptionRepository.existsByRegularUserIdAndWizardId(1, 2)).thenReturn(true);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("testuser");
        when(auth.getName()).thenReturn("testuser");

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When
        String view = userController.subscribeToWizard(2, redirectAttributes);

        // Then
        assertEquals("redirect:/users/2", view);
        verify(redirectAttributes, times(1)).addAttribute("error", "Already subscribed");
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should unsubscribe from wizard successfully")
    void testUnsubscribeFromWizardSuccess() {
        // Given
        Subscription subscription = new Subscription(testRegularUser, testWizardUser);
        subscription.setId(1);

        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testRegularUser));
        when(subscriptionRepository.findByRegularUserIdAndWizardId(1, 2)).thenReturn(Optional.of(subscription));

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("testuser");
        when(auth.getName()).thenReturn("testuser");

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When
        String view = userController.unsubscribeFromWizard(2, redirectAttributes);

        // Then
        assertEquals("redirect:/users/2", view);
        verify(subscriptionRepository, times(1)).delete(subscription);
    }

    @Test
    @DisplayName("Should redirect to login when not authenticated for unsubscribe")
    void testUnsubscribeFromWizardNotAuthenticated() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(auth.getPrincipal()).thenReturn("anonymousUser");

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When
        String view = userController.unsubscribeFromWizard(2, redirectAttributes);

        // Then
        assertEquals("redirect:/login", view);
        verify(subscriptionRepository, never()).delete(any(Subscription.class));
    }
}
