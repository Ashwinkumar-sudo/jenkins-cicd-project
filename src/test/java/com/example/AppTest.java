
package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    @Test
    void testApplicationMessage() {

        assertEquals(
            "Welcome to Jenkins CI/CD Pipeline!",
            App.getMessage()
        );
    }
}
