package com.example.controller;

import com.example.dto.UserLoginRequest;
import com.example.dto.UserRegistrationRequest;
import com.example.entity.User;
import com.example.service.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;

import jakarta.validation.Valid;

@Controller("/auth")
@Validated
@Secured(SecurityRule.IS_ANONYMOUS)
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Post("/register")
    public HttpResponse<User> register(@Body @Valid UserRegistrationRequest request) {
        try {
            User user = userService.registerUser(request);
            return HttpResponse.created(user);
        } catch (RuntimeException e) {
            return HttpResponse.badRequest();
        }
    }

    @Post("/login")
    public HttpResponse<String> login(@Body @Valid UserLoginRequest request) {
        // Login is handled by Micronaut Security JWT
        // This endpoint is just for documentation purposes
        // The actual login should be done via POST to /login with Basic Auth
        return HttpResponse.ok("Use POST /login with Basic Authentication");
    }
}