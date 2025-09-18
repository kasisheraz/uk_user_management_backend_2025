package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {

    @Test
    void testApplicationClassExists() {
        // Simple test that doesn't require full application startup
        Application app = new Application();
        assertNotNull(app);
    }

    @Test 
    void testMainMethodExists() {
        // Test that main method exists and can be accessed
        assertDoesNotThrow(() -> {
            Application.class.getDeclaredMethod("main", String[].class);
        });
    }
}