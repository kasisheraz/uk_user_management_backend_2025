package com.example.controller;

import com.example.dto.UserRegistrationRequest;
import com.example.entity.User;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class SimpleAuthControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testRegisterUser_Success() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser_" + System.currentTimeMillis());
        request.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        // When
        HttpRequest<UserRegistrationRequest> httpRequest = HttpRequest
            .POST("/auth/register", request);
        var response = client.toBlocking().exchange(httpRequest, User.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.body());
        
        User createdUser = response.body();
        assertEquals(request.getUsername(), createdUser.getUsername());
        assertEquals(request.getEmail(), createdUser.getEmail());
        assertEquals(request.getFirstName(), createdUser.getFirstName());
        assertEquals(request.getLastName(), createdUser.getLastName());
        assertTrue(createdUser.isEnabled());
    }

    @Test
    void testRegisterUser_InvalidData() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername(""); // Invalid - empty username
        request.setEmail("invalid-email"); // Invalid email format
        request.setPassword("123"); // Invalid - too short

        // When & Then
        HttpRequest<UserRegistrationRequest> httpRequest = HttpRequest
            .POST("/auth/register", request);

        assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(httpRequest, User.class);
        });
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
    void testRegisterUser_DuplicateUsername() {
        // Given
        String username = "duplicate_user_" + System.currentTimeMillis();
        
        // First registration
        UserRegistrationRequest firstRequest = new UserRegistrationRequest();
        firstRequest.setUsername(username);
        firstRequest.setEmail("first_" + System.currentTimeMillis() + "@example.com");
        firstRequest.setPassword("password123");

        HttpRequest<UserRegistrationRequest> httpRequest1 = HttpRequest
            .POST("/auth/register", firstRequest);
        client.toBlocking().exchange(httpRequest1, User.class);

        // Second registration with same username
        UserRegistrationRequest secondRequest = new UserRegistrationRequest();
        secondRequest.setUsername(username); // Same username
        secondRequest.setEmail("second_" + System.currentTimeMillis() + "@example.com");
        secondRequest.setPassword("password123");

        HttpRequest<UserRegistrationRequest> httpRequest2 = HttpRequest
            .POST("/auth/register", secondRequest);

        // Then
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class, 
            () -> client.toBlocking().exchange(httpRequest2, User.class)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }
}