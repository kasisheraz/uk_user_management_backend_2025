package com.example.controller;

import com.example.entity.User;
import com.example.service.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import org.reactivestreams.Publisher;

import java.security.Principal;

@Controller("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Get
    @Secured("ROLE_ADMIN")
    public MutableHttpResponse<Iterable<? extends Publisher<?>>> getAllUsers() {
        return null;
    }

    @Get("/me")
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public HttpResponse<User> getCurrentUser(Principal principal) {
        return userService.findByUsername(principal.getName())
            .map(HttpResponse::ok)
            .orElse(HttpResponse.notFound());
    }

    @Get("/{id}")
    @Secured("ROLE_ADMIN")
    public HttpResponse<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
            .map(HttpResponse::ok)
            .orElse(HttpResponse.notFound());
    }

    @Put("/{id}")
    @Secured("ROLE_ADMIN")
    public HttpResponse<User> updateUser(@PathVariable Long id, @Body User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return HttpResponse.ok(updatedUser);
        } catch (RuntimeException e) {
            return HttpResponse.notFound();
        }
    }

    @Delete("/{id}")
    @Secured("ROLE_ADMIN")
    public HttpResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return HttpResponse.noContent();
    }
}