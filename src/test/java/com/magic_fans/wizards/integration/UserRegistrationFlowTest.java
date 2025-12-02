package com.magic_fans.wizards.integration;

import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.repository.UserRepository;
import com.magic_fans.wizards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("User Registration and Authentication Flow Tests")
class UserRegistrationFlowTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "TestPassword123!";
    private static final String TEST_FIRSTNAME = "Test";
    private static final String TEST_LASTNAME = "User";
    private static final String TEST_SPECIALIZATION = "White Magic";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("User Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should successfully register a new user with valid data")
        void testSuccessfulUserRegistration() {
            // Given
            User newUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD,
                    TEST_FIRSTNAME, TEST_LASTNAME, TEST_SPECIALIZATION);

            // When
            User savedUser = userService.saveUser(newUser);

            // Then
            assertNotNull(savedUser.getId());
            assertEquals(TEST_USERNAME, savedUser.getUsername());
            assertEquals(TEST_EMAIL, savedUser.getEmail());
            assertEquals(TEST_FIRSTNAME, savedUser.getFirstName());
            assertEquals(TEST_LASTNAME, savedUser.getLastName());
            assertEquals(TEST_SPECIALIZATION, savedUser.getSpecialization());
            assertTrue(savedUser.isActive());
        }

        @Test
        @DisplayName("Should prevent duplicate username registration")
        void testPreventDuplicateUsername() {
            // Given
            User firstUser = new User(TEST_USERNAME, "first@example.com", TEST_PASSWORD,
                    TEST_FIRSTNAME, TEST_LASTNAME, TEST_SPECIALIZATION);
            userService.saveUser(firstUser);

            // When & Then
            assertTrue(userService.usernameExists(TEST_USERNAME));
            assertFalse(userService.usernameExists("different_username"));
        }

        @Test
        @DisplayName("Should prevent duplicate email registration")
        void testPreventDuplicateEmail() {
            // Given
            User firstUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD,
                    TEST_FIRSTNAME, TEST_LASTNAME, TEST_SPECIALIZATION);
            userService.saveUser(firstUser);

            // When & Then
            assertTrue(userService.emailExists(TEST_EMAIL));
            assertFalse(userService.emailExists("different@example.com"));
        }

        @Test
        @DisplayName("Should encode password correctly")
        void testPasswordEncoding() {
            // Given
            User user = new User(TEST_USERNAME, TEST_EMAIL,
                    passwordEncoder.encode(TEST_PASSWORD),
                    TEST_FIRSTNAME, TEST_LASTNAME, TEST_SPECIALIZATION);

            // When
            User savedUser = userService.saveUser(user);

            // Then
            assertNotEquals(TEST_PASSWORD, savedUser.getPassword());
            assertTrue(passwordEncoder.matches(TEST_PASSWORD, savedUser.getPassword()));
        }
    }

    @Nested
    @DisplayName("User Retrieval Tests")
    class RetrievalTests {

        @BeforeEach
        void setUp() {
            User user = new User(TEST_USERNAME, TEST_EMAIL,
                    passwordEncoder.encode(TEST_PASSWORD),
                    TEST_FIRSTNAME, TEST_LASTNAME, TEST_SPECIALIZATION);
            userRepository.save(user);
        }

        @Test
        @DisplayName("Should retrieve user by username")
        void testGetUserByUsername() {
            // When
            var user = userService.getUserByUsername(TEST_USERNAME);

            // Then
            assertTrue(user.isPresent());
            assertEquals(TEST_EMAIL, user.get().getEmail());
        }

        @Test
        @DisplayName("Should retrieve user by email")
        void testGetUserByEmail() {
            // When
            var user = userService.getUserByEmail(TEST_EMAIL);

            // Then
            assertTrue(user.isPresent());
            assertEquals(TEST_USERNAME, user.get().getUsername());
        }

        @Test
        @DisplayName("Should retrieve user by ID")
        void testGetUserById() {
            // Given
            var savedUser = userService.getUserByUsername(TEST_USERNAME).orElseThrow();
            int userId = savedUser.getId();

            // When
            var user = userService.getUserById(userId);

            // Then
            assertTrue(user.isPresent());
            assertEquals(TEST_USERNAME, user.get().getUsername());
        }

        @Test
        @DisplayName("Should return empty when user not found")
        void testUserNotFound() {
            // When
            var user = userService.getUserByUsername("nonexistent");

            // Then
            assertTrue(user.isEmpty());
        }
    }

    @Nested
    @DisplayName("User Update Tests")
    class UpdateTests {

        @BeforeEach
        void setUp() {
            User user = new User(TEST_USERNAME, TEST_EMAIL,
                    passwordEncoder.encode(TEST_PASSWORD),
                    TEST_FIRSTNAME, TEST_LASTNAME, TEST_SPECIALIZATION);
            userRepository.save(user);
        }

        @Test
        @DisplayName("Should successfully update user information")
        void testUpdateUser() {
            // Given
            User user = userService.getUserByUsername(TEST_USERNAME).orElseThrow();
            user.setFirstName("Updated");
            user.setLastName("Name");
            user.setSpecialization("Black Magic");

            // When
            User updatedUser = userService.updateUser(user);

            // Then
            assertEquals("Updated", updatedUser.getFirstName());
            assertEquals("Name", updatedUser.getLastName());
            assertEquals("Black Magic", updatedUser.getSpecialization());
        }

        @Test
        @DisplayName("Should deactivate user account")
        void testDeactivateUser() {
            // Given
            User user = userService.getUserByUsername(TEST_USERNAME).orElseThrow();
            user.setActive(false);

            // When
            User updatedUser = userService.updateUser(user);

            // Then
            assertFalse(updatedUser.isEnabled());
        }
    }

    @Nested
    @DisplayName("User Deletion Tests")
    class DeletionTests {

        @BeforeEach
        void setUp() {
            User user = new User(TEST_USERNAME, TEST_EMAIL,
                    passwordEncoder.encode(TEST_PASSWORD),
                    TEST_FIRSTNAME, TEST_LASTNAME, TEST_SPECIALIZATION);
            userRepository.save(user);
        }

        @Test
        @DisplayName("Should successfully delete user")
        void testDeleteUser() {
            // Given
            User user = userService.getUserByUsername(TEST_USERNAME).orElseThrow();
            int userId = user.getId();

            // When
            userService.deleteUser(userId);

            // Then
            assertFalse(userService.getUserById(userId).isPresent());
        }
    }

    @Nested
    @DisplayName("User Security Tests")
    class SecurityTests {

        @BeforeEach
        void setUp() {
            User user = new User(TEST_USERNAME, TEST_EMAIL,
                    passwordEncoder.encode(TEST_PASSWORD),
                    TEST_FIRSTNAME, TEST_LASTNAME, TEST_SPECIALIZATION);
            userRepository.save(user);
        }

        @Test
        @DisplayName("Should have ROLE_USER authority")
        void testUserHasCorrectRole() {
            // When
            User user = userService.getUserByUsername(TEST_USERNAME).orElseThrow();

            // Then
            assertTrue(user.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        }

        @Test
        @DisplayName("Should be enabled when active is true")
        void testUserEnabledWhenActive() {
            // When
            User user = userService.getUserByUsername(TEST_USERNAME).orElseThrow();

            // Then
            assertTrue(user.isEnabled());
            assertTrue(user.isAccountNonExpired());
            assertTrue(user.isAccountNonLocked());
            assertTrue(user.isCredentialsNonExpired());
        }

        @Test
        @DisplayName("Should not be enabled when active is false")
        void testUserDisabledWhenInactive() {
            // Given
            User user = userService.getUserByUsername(TEST_USERNAME).orElseThrow();
            user.setActive(false);
            userService.updateUser(user);

            // When
            User updatedUser = userService.getUserByUsername(TEST_USERNAME).orElseThrow();

            // Then
            assertFalse(updatedUser.isEnabled());
        }
    }

    @Nested
    @DisplayName("User Query Tests")
    class QueryTests {

        @BeforeEach
        void setUp() {
            userRepository.save(new User("user1", "user1@test.com", "pwd1",
                    "John", "Doe", "White Magic"));
            userRepository.save(new User("user2", "user2@test.com", "pwd2",
                    "Jane", "Smith", "Black Magic"));
            userRepository.save(new User("user3", "user3@test.com", "pwd3",
                    "Bob", "Johnson", "Gray Magic"));
        }

        @Test
        @DisplayName("Should retrieve all users")
        void testGetAllUsers() {
            // When
            var users = userService.getAllUsers();

            // Then
            assertEquals(3, users.size());
        }

        @Test
        @DisplayName("Should verify existence queries work correctly")
        void testExistenceQueries() {
            // When & Then
            assertTrue(userService.usernameExists("user1"));
            assertTrue(userService.emailExists("user2@test.com"));
            assertFalse(userService.usernameExists("nonexistent"));
            assertFalse(userService.emailExists("nonexistent@test.com"));
        }
    }
}