package com.magic_fans.wizards.service;

import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password123", "John", "Doe", "White Magic");
        testUser.setId(1);
    }

    @Test
    void testSaveUser() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        User savedUser = userService.saveUser(testUser);

        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testGetUserById_Found() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        Optional<User> foundUser = userService.getUserById(1);

        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getUserById(999);

        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findById(999);
    }

    @Test
    void testGetUserByUsername_Found() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> foundUser = userService.getUserByUsername("testuser");

        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getUserByUsername("nonexistent");

        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void testGetUserByEmail_Found() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        Optional<User> foundUser = userService.getUserByEmail("test@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getUserByEmail("nonexistent@example.com");

        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void testUsernameExists_True() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        boolean exists = userService.usernameExists("testuser");

        assertTrue(exists);
        verify(userRepository, times(1)).existsByUsername("testuser");
    }

    @Test
    void testUsernameExists_False() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        boolean exists = userService.usernameExists("newuser");

        assertFalse(exists);
        verify(userRepository, times(1)).existsByUsername("newuser");
    }

    @Test
    void testEmailExists_True() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean exists = userService.emailExists("test@example.com");

        assertTrue(exists);
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    void testEmailExists_False() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        boolean exists = userService.emailExists("new@example.com");

        assertFalse(exists);
        verify(userRepository, times(1)).existsByEmail("new@example.com");
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1);

        userService.deleteUser(1);

        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    void testUpdateUser() {
        testUser.setFirstName("Jane");
        when(userRepository.save(testUser)).thenReturn(testUser);

        User updatedUser = userService.updateUser(testUser);

        assertEquals("Jane", updatedUser.getFirstName());
        verify(userRepository, times(1)).save(testUser);
    }
}