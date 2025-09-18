package com.example.controller;

import com.example.dto.UserRegistrationRequest;
import com.example.entity.User;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class AuthControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    ObjectMapper objectMapper;

    private UserRegistrationRequest validRegistrationRequest;
    private UserRegistrationRequest invalidRegistrationRequest;

    @BeforeEach
    void setUp() {
        validRegistrationRequest = new UserRegistrationRequest();
        validRegistrationRequest.setUsername("testuser");
        validRegistrationRequest.setEmail("test@example.com");
        validRegistrationRequest.setPassword("password123");
        validRegistrationRequest.setFirstName("Test");
        validRegistrationRequest.setLastName("User");

        invalidRegistrationRequest = new UserRegistrationRequest();
        invalidRegistrationRequest.setUsername(""); // Invalid - empty username
        invalidRegistrationRequest.setEmail("invalid-email"); // Invalid email format
        invalidRegistrationRequest.setPassword("123"); // Invalid - too short
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        // Create unique username for this test
        validRegistrationRequest.setUsername("testuser_" + System.currentTimeMillis());
        validRegistrationRequest.setEmail("test_" + System.currentTimeMillis() + "@example.com");

        // When
        HttpRequest<UserRegistrationRequest> request = HttpRequest
            .POST("/auth/register", validRegistrationRequest);
        
        var response = client.toBlocking().exchange(request, User.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.body());
        
        User createdUser = response.body();
        assertEquals(validRegistrationRequest.getUsername(), createdUser.getUsername());
        assertEquals(validRegistrationRequest.getEmail(), createdUser.getEmail());
        assertEquals(validRegistrationRequest.getFirstName(), createdUser.getFirstName());
        assertEquals(validRegistrationRequest.getLastName(), createdUser.getLastName());
        assertTrue(createdUser.isEnabled());
        assertNull(createdUser.getPassword()); // Password should not be returned
    }

    @Test
    void testRegisterUser_ValidationError() {
        // When & Then
        HttpRequest<UserRegistrationRequest> request = HttpRequest
            .POST("/auth/register", invalidRegistrationRequest);

        assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, User.class);
        });
    }

    @Test
    void testRegisterUser_DuplicateUsername() throws Exception {
        // First registration
        validRegistrationRequest.setUsername("duplicate_user");
        validRegistrationRequest.setEmail("first@example.com");

        HttpRequest<UserRegistrationRequest> firstRequest = HttpRequest
            .POST("/auth/register", validRegistrationRequest);
        client.toBlocking().exchange(firstRequest, User.class);

        // Second registration with same username
        validRegistrationRequest.setEmail("second@example.com");
        HttpRequest<UserRegistrationRequest> secondRequest = HttpRequest
            .POST("/auth/register", validRegistrationRequest);

        // Then
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class, 
            () -> client.toBlocking().exchange(secondRequest, User.class)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testRegisterUser_DuplicateEmail() throws Exception {
        // First registration
        validRegistrationRequest.setUsername("first_user");
        validRegistrationRequest.setEmail("duplicate@example.com");

        HttpRequest<UserRegistrationRequest> firstRequest = HttpRequest
            .POST("/auth/register", validRegistrationRequest);
        client.toBlocking().exchange(firstRequest, User.class);

        // Second registration with same email
        validRegistrationRequest.setUsername("second_user");
        HttpRequest<UserRegistrationRequest> secondRequest = HttpRequest
            .POST("/auth/register", validRegistrationRequest);

        // Then
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class, 
            () -> client.toBlocking().exchange(secondRequest, User.class)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testLoginEndpoint() {
        // When
        HttpRequest<String> request = HttpRequest.POST("/auth/login", "{}");
        var response = client.toBlocking().exchange(request, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        assertTrue(response.body().contains("Basic Authentication"));
    }

    @Test
    void testRegisterUser_MinimalData() throws Exception {
        // Given - only required fields
        UserRegistrationRequest minimalRequest = new UserRegistrationRequest();
        minimalRequest.setUsername("minimal_" + System.currentTimeMillis());
        minimalRequest.setEmail("minimal_" + System.currentTimeMillis() + "@example.com");
        minimalRequest.setPassword("password123");
        // firstName and lastName are optional

        // When
        HttpRequest<UserRegistrationRequest> request = HttpRequest
            .POST("/auth/register", minimalRequest);
        var response = client.toBlocking().exchange(request, User.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatus());
        User createdUser = response.body();
        assertNotNull(createdUser);
        assertEquals(minimalRequest.getUsername(), createdUser.getUsername());
        assertEquals(minimalRequest.getEmail(), createdUser.getEmail());
    }

    @Test
    void testRegisterUser_EmptyBody() {
        // When & Then
        HttpRequest<String> request = HttpRequest.POST("/auth/register", "{}");
        
        assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, User.class);
        });
    }

    @Test
    void testRegisterUser_NullBody() {
        // When & Then
        HttpRequest<UserRegistrationRequest> request = HttpRequest
            .POST("/auth/register", (UserRegistrationRequest) null);
        
        assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, User.class);
        });
    }
}