package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.dto.UserProfileDTO;
import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProfileFeedController Unit Tests")
class ProfileFeedControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ProfileFeedController profileFeedController;

    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        testUsers = createTestUsers();
    }

    private List<User> createTestUsers() {
        List<User> users = new ArrayList<>();

        // Create 2 regular users
        User regular1 = new User("user1", "user1@test.com", "pwd1", "John", "Doe", "None");
        regular1.setId(1);
        regular1.setRole("regular");
        regular1.setActive(true);
        users.add(regular1);

        User regular2 = new User("user2", "user2@test.com", "pwd2", "Jane", "Smith", "None");
        regular2.setId(2);
        regular2.setRole("regular");
        regular2.setActive(true);
        users.add(regular2);

        // Create 3 wizard users
        User wizard1 = new User("merlin", "merlin@test.com", "pwd3", "Merlin", "The Great", "White Magic");
        wizard1.setId(3);
        wizard1.setRole("wizard");
        wizard1.setActive(true);
        users.add(wizard1);

        User wizard2 = new User("morgana", "morgana@test.com", "pwd4", "Morgana", "The Wise", "Black Magic");
        wizard2.setId(4);
        wizard2.setRole("wizard");
        wizard2.setActive(true);
        users.add(wizard2);

        User wizard3 = new User("gandalf", "gandalf@test.com", "pwd5", "Gandalf", "The Grey", "Gray Magic");
        wizard3.setId(5);
        wizard3.setRole("wizard");
        wizard3.setActive(true);
        users.add(wizard3);

        return users;
    }

    @Test
    @DisplayName("Should return paginated profiles feed for anonymous user (shows only wizards)")
    void testGetProfilesFeedAnonymousUser() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(userService.getAllUsers()).thenReturn(testUsers);

        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.getProfilesFeed(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size()); // Only wizards

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Should apply offset and limit pagination correctly")
    void testGetProfilesFeedWithOffsetAndLimit() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(userService.getAllUsers()).thenReturn(testUsers);

        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.getProfilesFeed(1, 2);

        // Then
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size()); // Skip 1, take 2
    }

    @Test
    @DisplayName("Should cap limit to maximum 50")
    void testGetProfilesFeedLimitCapped() {
        // Given
        List<User> manyUsers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User user = new User("wizard" + i, "wizard" + i + "@test.com", "pwd", "Wizard", String.valueOf(i), "Magic");
            user.setId(i);
            user.setRole("wizard");
            user.setActive(true);
            manyUsers.add(user);
        }
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(userService.getAllUsers()).thenReturn(manyUsers);

        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.getProfilesFeed(0, 100);

        // Then
        assertNotNull(response.getBody());
        assertEquals(50, response.getBody().size()); // Capped at 50
    }

    @Test
    @DisplayName("Should set default limit to 10 when less than 1")
    void testGetProfilesFeedDefaultLimit() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(userService.getAllUsers()).thenReturn(testUsers);

        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.getProfilesFeed(0, 0);

        // Then
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size()); // All 3 wizards fit in default 10
    }

    @Test
    @DisplayName("Should set offset to 0 when negative")
    void testGetProfilesFeedNegativeOffset() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(userService.getAllUsers()).thenReturn(testUsers);

        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.getProfilesFeed(-5, 10);

        // Then
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size()); // All wizards from start
    }

    @Test
    @DisplayName("Should filter profiles by specialization")
    void testGetProfilesBySpecialization() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(userService.getAllUsers()).thenReturn(testUsers);

        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.getProfilesBySpecialization("White Magic", 0, 10);

        // Then
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("White Magic", response.getBody().get(0).getSpecialization());

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Should handle case-insensitive specialization search")
    void testGetProfilesBySpecializationCaseInsensitive() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(userService.getAllUsers()).thenReturn(testUsers);

        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.getProfilesBySpecialization("white magic", 0, 10);

        // Then
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should return empty list for non-existent specialization")
    void testGetProfilesBySpecializationNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(userService.getAllUsers()).thenReturn(testUsers);

        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.getProfilesBySpecialization("Nonexistent Magic", 0, 10);

        // Then
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    @DisplayName("Should search profiles by username")
    void testSearchProfilesByUsername() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(userService.getAllUsers()).thenReturn(testUsers);

        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.searchProfiles("merlin", 0, 10);

        // Then
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("merlin", response.getBody().get(0).getUsername());
    }

    @Test
    @DisplayName("Should search profiles by first name")
    void testSearchProfilesByFirstName() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(userService.getAllUsers()).thenReturn(testUsers);

        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.searchProfiles("Gandalf", 0, 10);

        // Then
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should search profiles by last name")
    void testSearchProfilesByLastName() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(userService.getAllUsers()).thenReturn(testUsers);

        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.searchProfiles("Great", 0, 10);

        // Then
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should handle case-insensitive search")
    void testSearchProfilesCaseInsensitive() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(userService.getAllUsers()).thenReturn(testUsers);

        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.searchProfiles("MERLIN", 0, 10);

        // Then
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should return bad request for empty search query")
    void testSearchProfilesEmptyQuery() {
        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.searchProfiles("", 0, 10);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).getAllUsers();
    }

    @Test
    @DisplayName("Should return bad request for null search query")
    void testSearchProfilesNullQuery() {
        // When
        ResponseEntity<List<UserProfileDTO>> response = profileFeedController.searchProfiles(null, 0, 10);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return total count of active users")
    void testGetTotalProfilesCount() {
        // Given
        when(userService.getAllUsers()).thenReturn(testUsers);

        // When
        ResponseEntity<Integer> response = profileFeedController.getTotalProfilesCount();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody()); // All 5 users are active

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Should return 0 count when no users exist")
    void testGetTotalProfilesCountEmpty() {
        // Given
        when(userService.getAllUsers()).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<Integer> response = profileFeedController.getTotalProfilesCount();

        // Then
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody());
    }

    @Test
    @DisplayName("Should only count active users in total count")
    void testGetTotalProfilesCountInactiveUsers() {
        // Given
        List<User> usersWithInactive = new ArrayList<>(testUsers);
        User inactiveUser = new User("inactive", "inactive@test.com", "pwd", "Inactive", "User", "None");
        inactiveUser.setId(6);
        inactiveUser.setRole("regular");
        inactiveUser.setActive(false);
        usersWithInactive.add(inactiveUser);

        when(userService.getAllUsers()).thenReturn(usersWithInactive);

        // When
        ResponseEntity<Integer> response = profileFeedController.getTotalProfilesCount();

        // Then
        assertEquals(5, response.getBody()); // Only active users
    }
}
