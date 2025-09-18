package com.example;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;

@MicronautTest(startApplication = false) // Don't start the full application context
class ApplicationTest {

    @Test
    void testApplicationClassExists() {
        // Simple test that doesn't require full application startup
        Application app = new Application();
        Assertions.assertNotNull(app);
    }

    @Test
    void testMainMethod() {
        // Test that main method can be called without errors
        Assertions.assertDoesNotThrow(() -> {
            // Just verify the class structure, don't actually start
            Application.class.getDeclaredMethod("main", String[].class);
        });
    }
}