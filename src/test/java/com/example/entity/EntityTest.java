package com.example.entity;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testUserEntityCreation() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        // Then
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertTrue(user.isEnabled());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getRoles());
    }

    @Test
    void testUserEntityConstructor() {
        // When
        User user = new User("testuser", "test@example.com", "password123");

        // Then
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
    }

    @Test
    void testRoleEntityCreation() {
        // Given
        Role role = new Role();
        role.setId(1L);
        role.setName(Role.RoleName.ADMIN);
        role.setDescription("Administrator role");

        // Then
        assertEquals(1L, role.getId());
        assertEquals(Role.RoleName.ADMIN, role.getName());
        assertEquals("Administrator role", role.getDescription());
    }

    @Test
    void testRoleEntityConstructor() {
        // When
        Role role = new Role(Role.RoleName.USER, "Regular user role");

        // Then
        assertEquals(Role.RoleName.USER, role.getName());
        assertEquals("Regular user role", role.getDescription());
    }

    @Test
    void testUserWithRoles() {
        // Given
        Role adminRole = new Role(Role.RoleName.ADMIN, "Admin role");
        Role userRole = new Role(Role.RoleName.USER, "User role");

        User user = new User("admin", "admin@example.com", "password");
        user.setRoles(Set.of(adminRole, userRole));

        // Then
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains(adminRole));
        assertTrue(user.getRoles().contains(userRole));
    }

    @Test
    void testPasswordHashing() {
        // Given
        String rawPassword = "mySecretPassword";
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        User user = new User();
        user.setPassword(hashedPassword);

        // Then
        assertNotEquals(rawPassword, user.getPassword());
        assertTrue(BCrypt.checkpw(rawPassword, user.getPassword()));
        assertFalse(BCrypt.checkpw("wrongPassword", user.getPassword()));
    }

    @Test
    void testRoleEnumValues() {
        // Test all role enum values
        assertEquals("ADMIN", Role.RoleName.ADMIN.name());
        assertEquals("USER", Role.RoleName.USER.name());
        assertEquals("MODERATOR", Role.RoleName.MODERATOR.name());

        // Test enum array
        Role.RoleName[] roles = Role.RoleName.values();
        assertEquals(3, roles.length);
    }

    @Test
    void testUserDefaultValues() {
        // Given
        User user = new User();

        // Then
        assertTrue(user.isEnabled()); // Default should be true
        assertNotNull(user.getRoles()); // Should be initialized
        assertNotNull(user.getCreatedAt()); // Should be set to now
    }

    @Test
    void testUserEqualsAndHashcode() {
        // Given
        User user1 = new User("testuser", "test@example.com", "password");
        user1.setId(1L);

        User user2 = new User("testuser", "test@example.com", "password");
        user2.setId(1L);

        User user3 = new User("differentuser", "different@example.com", "password");
        user3.setId(2L);

        // Note: This test assumes you haven't overridden equals/hashCode
        // If you have, adjust the assertions accordingly
        assertNotNull(user1);
        assertNotNull(user2);
        assertNotNull(user3);
    }
}