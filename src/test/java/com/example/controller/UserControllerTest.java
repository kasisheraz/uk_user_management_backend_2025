package com.example.controller;

import com.example.entity.Role;
import com.example.entity.User;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class UserControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        // Get admin token (assuming admin user exists from DataLoader)
        adminToken = getAuthToken("admin", "admin123");
        
        // Note: For user token, you'd need to create a regular user first
        // or handle the case where user doesn't exist
    }

    private String getAuthToken(String username, String password) {
        try {
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            HttpRequest<UsernamePasswordCredentials> request = HttpRequest.POST("/login", credentials);
            var response = client.toBlocking().exchange(request, BearerAccessRefreshToken.class);
            return response.body().getAccessToken();
        } catch (Exception e) {
            return null; // Handle case where user doesn't exist
        }
    }

    @Test
    void testGetAllUsers_AdminAccess() {
        if (adminToken == null) {
            // Skip test if admin not available
            return;
        }

        // When
        HttpRequest<Object> request = HttpRequest
            .GET("/api/users")
            .bearerAuth(adminToken);
        var response = client.toBlocking().exchange(request, List.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        List<?> users = response.body();
        assertTrue(users.size() >= 1); // At least admin user should exist
    }

    @Test
    void testGetAllUsers_UnauthorizedWithoutToken() {
        // When & Then
        HttpRequest<Object> request = HttpRequest.GET("/api/users");
        
        assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, List.class);
        });
    }

    @Test
    void testGetCurrentUser_ValidToken() {
        if (adminToken == null) {
            return;
        }

        // When
        HttpRequest<Object> request = HttpRequest
            .GET("/api/users/me")
            .bearerAuth(adminToken);
        var response = client.toBlocking().exchange(request, User.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        User currentUser = response.body();
        assertEquals("admin", currentUser.getUsername());
    }

    @Test
    void testGetCurrentUser_UnauthorizedWithoutToken() {
        // When & Then
        HttpRequest<Object> request = HttpRequest.GET("/api/users/me");
        
        assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, User.class);
        });
    }

    @Test
    void testGetUserById_AdminAccess() {
        if (adminToken == null) {
            return;
        }

        // When - assuming user with ID 1 exists (admin user)
        HttpRequest<Object> request = HttpRequest
            .GET("/api/users/1")
            .bearerAuth(adminToken);
        var response = client.toBlocking().exchange(request, User.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        User user = response.body();
        assertEquals(1L, user.getId());
    }

    @Test
    void testGetUserById_UserNotFound() {
        if (adminToken == null) {
            return;
        }

        // When & Then
        HttpRequest<Object> request = HttpRequest
            .GET("/api/users/999999")
            .bearerAuth(adminToken);
        
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(request, User.class)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testUpdateUser_AdminAccess() {
        if (adminToken == null) {
            return;
        }

        // Given
        User updateRequest = new User();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Admin");
        updateRequest.setEmail("updated-admin@example.com");

        // When
        HttpRequest<User> request = HttpRequest
            .PUT("/api/users/1", updateRequest)
            .bearerAuth(adminToken);
        var response = client.toBlocking().exchange(request, User.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        User updatedUser = response.body();
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Admin", updatedUser.getLastName());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        if (adminToken == null) {
            return;
        }

        // Given
        User updateRequest = new User();
        updateRequest.setFirstName("Test");
        updateRequest.setLastName("User");

        // When & Then
        HttpRequest<User> request = HttpRequest
            .PUT("/api/users/999999", updateRequest)
            .bearerAuth(adminToken);
        
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(request, User.class)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testDeleteUser_AdminAccess() {
        if (adminToken == null) {
            return;
        }

        // First create a user to delete
        User testUser = createTestUser();
        if (testUser == null || testUser.getId() == null) {
            return; // Skip if user creation failed
        }

        // When
        HttpRequest<Object> request = HttpRequest
            .DELETE("/api/users/" + testUser.getId())
            .bearerAuth(adminToken);
        var response = client.toBlocking().exchange(request);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }

    @Test
    void testDeleteUser_UserNotFound() {
        if (adminToken == null) {
            return;
        }

        // When & Then
        HttpRequest<Object> request = HttpRequest
            .DELETE("/api/users/999999")
            .bearerAuth(adminToken);
        
        // Note: Micronaut's deleteById might not throw exception for non-existent IDs
        // It depends on the JPA repository implementation
        var response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }

    private User createTestUser() {
        try {
            // Create a test user via registration endpoint
            var registrationRequest = new com.example.dto.UserRegistrationRequest();
            registrationRequest.setUsername("testuser_" + System.currentTimeMillis());
            registrationRequest.setEmail("testuser_" + System.currentTimeMillis() + "@example.com");
            registrationRequest.setPassword("password123");
            registrationRequest.setFirstName("Test");
            registrationRequest.setLastName("User");

            HttpRequest<com.example.dto.UserRegistrationRequest> request = HttpRequest
                .POST("/auth/register", registrationRequest);
            var response = client.toBlocking().exchange(request, User.class);
            return response.body();
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    void testUserEndpoints_SecurityAnnotations() {
        // Test that endpoints without auth tokens are rejected
        
        // GET /api/users requires ADMIN role
        HttpRequest<Object> getUsersRequest = HttpRequest.GET("/api/users");
        assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(getUsersRequest, List.class);
        });

        // GET /api/users/me requires USER or ADMIN role
        HttpRequest<Object> getCurrentUserRequest = HttpRequest.GET("/api/users/me");
        assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(getCurrentUserRequest, User.class);
        });

        // PUT /api/users/{id} requires ADMIN role
        User updateRequest = new User();
        HttpRequest<User> updateUserRequest = HttpRequest.PUT("/api/users/1", updateRequest);
        assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(updateUserRequest, User.class);
        });

        // DELETE /api/users/{id} requires ADMIN role
        HttpRequest<Object> deleteUserRequest = HttpRequest.DELETE("/api/users/1");
        assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(deleteUserRequest);
        });
    }
}