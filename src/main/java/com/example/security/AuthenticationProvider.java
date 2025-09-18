package com.example.security;

import com.example.entity.Role;
import com.example.entity.User;
import com.example.service.UserService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class AuthenticationProvider implements io.micronaut.security.authentication.provider.AuthenticationProvider {

    private final UserService userService;

    public AuthenticationProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AuthenticationResponse authenticate(Object requestContext,
                                             AuthenticationRequest authenticationRequest) {
        String username = authenticationRequest.getIdentity().toString();
        String password = authenticationRequest.getSecret().toString();

        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            return AuthenticationResponse.failure(AuthenticationFailureReason.USER_NOT_FOUND);
        }

        if (!user.isEnabled() || !userService.validatePassword(password, user.getPassword())) {
            return AuthenticationResponse.failure(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
        }

        List<String> roles = new ArrayList<>();
        for (Role role : user.getRoles()) {
            roles.add("ROLE_" + role.getName().toString());
        }

        return AuthenticationResponse.success(username, roles);
    }
}