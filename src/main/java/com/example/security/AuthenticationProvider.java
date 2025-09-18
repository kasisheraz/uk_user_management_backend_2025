package com.example.security;

import com.example.entity.Role;
import com.example.entity.User;
import com.example.service.UserService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Singleton
public abstract class AuthenticationProvider implements io.micronaut.security.authentication.provider.AuthenticationProvider {

    private final UserService userService;
    private final Executor virtualThreadExecutor;

    public AuthenticationProvider(UserService userService) {
        this.userService = userService;
        // Create virtual thread executor for Java 21
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest,
                                                        AuthenticationRequest<?, ?> authenticationRequest) {
        
        // Use CompletableFuture with virtual threads instead of Reactor
        CompletableFuture<Serializable> future = CompletableFuture.supplyAsync(() -> {
            String username = authenticationRequest.getIdentity().toString();
            String password = authenticationRequest.getSecret().toString();

            return userService.findByUsername(username)
                .map(user -> {
                    if (user.isEnabled() && userService.validatePassword(password, user.getPassword())) {
                        List<String> roles = new ArrayList<>();
                        for (Role role : user.getRoles()) {
                            roles.add("ROLE_" + role.getName().toString());
                        }
                        return AuthenticationResponse.success(username, roles);
                    } else {
                        return AuthenticationResponse.exception(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
                    }
                })
                .orElse(AuthenticationResponse.exception(AuthenticationFailureReason.USER_NOT_FOUND));
        }, virtualThreadExecutor);

        // Convert CompletableFuture to Publisher for Micronaut compatibility
        return subscriber -> {
            future.whenCompleteAsync((result, throwable) -> {
                if (throwable != null) {
                    subscriber.onError(throwable);
                } else {
                    subscriber.onNext((AuthenticationResponse) result);
                    subscriber.onComplete();
                }
            }, virtualThreadExecutor);
        };
    }
}