package com.dapp.futbol_api.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConstantsTest {

    @Test
    void testConstructorShouldThrowException() {
        // Arrange: Get the private constructor via reflection
        Constructor<SecurityConstants> constructor;
        try {
            constructor = SecurityConstants.class.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            fail("Private constructor for SecurityConstants not found.", e);
            return;
        }
        constructor.setAccessible(true);

        // Act & Assert: Verify that invoking the constructor throws the expected exception
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance,
                "Expected constructor to throw an exception, but it didn't");

        assertInstanceOf(IllegalStateException.class, thrown.getCause(), "The cause should be an IllegalStateException.");
        assertEquals("Utility class", thrown.getCause().getMessage(), "The exception message does not match.");
    }
}