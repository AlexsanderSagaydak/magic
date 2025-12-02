package com.magic_fans.wizards.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("john_doe", "john@example.com", "password123", "John", "Doe", "Black Magic");
    }

    @Test
    void testUserConstruction() {
        assertEquals("john_doe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("Black Magic", user.getSpecialization());
        assertTrue(user.isActive());
    }

    @Test
    void testSettersAndGetters() {
        user.setId(5);
        user.setUsername("new_username");
        user.setEmail("new@example.com");
        user.setPassword("newpassword");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setSpecialization("White Magic");
        user.setActive(false);

        assertEquals(5, user.getId());
        assertEquals("new_username", user.getUsername());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("newpassword", user.getPassword());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("White Magic", user.getSpecialization());
        assertFalse(user.isActive());
    }

    @Test
    void testGetAuthorities() {
        var authorities = user.getAuthorities();

        assertNotNull(authorities);
        assertFalse(authorities.isEmpty());
        assertTrue(authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_USER")));
    }

    @Test
    void testUserDetailsMethods() {
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void testIsEnabledDependsOnActive() {
        user.setActive(true);
        assertTrue(user.isEnabled());

        user.setActive(false);
        assertFalse(user.isEnabled());
    }

    @Test
    void testEmptyConstructor() {
        User emptyUser = new User();
        assertNull(emptyUser.getUsername());
        assertNull(emptyUser.getEmail());
        assertNull(emptyUser.getPassword());
    }

    @Test
    void testUserWithDifferentSpecializations() {
        User[] users = {
                new User("user1", "user1@test.com", "pwd1", "A", "B", "White Magic"),
                new User("user2", "user2@test.com", "pwd2", "C", "D", "Black Magic"),
                new User("user3", "user3@test.com", "pwd3", "E", "F", "Gray Magic"),
                new User("user4", "user4@test.com", "pwd4", "G", "H", "Elemental Magic"),
                new User("user5", "user5@test.com", "pwd5", "I", "J", "Time Magic"),
                new User("user6", "user6@test.com", "pwd6", "K", "L", "Illusion Magic"),
                new User("user7", "user7@test.com", "pwd7", "M", "N", "Healing Magic")
        };

        String[] expectedSpecializations = {
                "White Magic", "Black Magic", "Gray Magic", "Elemental Magic",
                "Time Magic", "Illusion Magic", "Healing Magic"
        };

        for (int i = 0; i < users.length; i++) {
            assertEquals(expectedSpecializations[i], users[i].getSpecialization());
            assertTrue(users[i].isEnabled());
        }
    }
}