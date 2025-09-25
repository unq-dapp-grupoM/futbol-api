package com.dapp.futbol_api.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testBuilderShouldCreateUserCorrectly() {
        String email = "test@example.com";
        String password = "password123";
        Role role = Role.USER;
        Integer id = 1;

        User user = User.builder()
                .id(id)
                .email(email)
                .password(password)
                .role(role)
                .build();

        assertNotNull(user);
        assertEquals(id, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
    }

    @Test
    void testGetUsernameShouldReturnEmail() {
        String expectedEmail = "user@test.com";
        User user = User.builder().email(expectedEmail).build();

        String username = user.getUsername();

        assertEquals(expectedEmail, username);
    }

    @Test
    void testGetAuthoritiesShouldReturnCorrectRole() {
        User user = User.builder().role(Role.USER).build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("USER")));
    }

    @Test
    void testUserDetailsMethodsShouldReturnTrue() {
        User user = new User();

        // These methods are not overridden, so they use the default implementation
        // from the UserDetails interface, which returns true.
        assertTrue(user.isAccountNonExpired(), "Account should be non-expired by default");
        assertTrue(user.isAccountNonLocked(), "Account should be non-locked by default");
        assertTrue(user.isCredentialsNonExpired(), "Credentials should be non-expired by default");
        assertTrue(user.isEnabled(), "Account should be enabled by default");
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        User user = new User();
        Integer id = 10;
        String email = "setter@test.com";
        String password = "setterPassword";
        Role role = Role.ADMIN; // We assume the ADMIN role exists for a more complete test.

        user.setId(id);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);

        assertEquals(id, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
    }

    @Test
    void testAllArgsConstructor() {
        Integer id = 20;
        String email = "allargs@test.com";
        String password = "allArgsPassword";
        Role role = Role.USER;

        User user = new User(id, email, password, role);

        assertEquals(id, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
    }

    @Test
    void testEqualsAndHashCodeShouldBehaveCorrectly() {
        // Lombok's @Data annotation generates `equals` and `hashCode` based on all fields.
        User user1 = new User(1, "user@example.com", "pass", Role.USER);
        User user2 = new User(1, "user@example.com", "pass", Role.USER);
        User user3 = new User(2, "another@example.com", "pass123", Role.ADMIN);
        User user4 = new User(1, "user@example.com", "different-pass", Role.USER);

        assertEquals(user1, user2, "Users with same data should be equal");
        assertNotEquals(user1, user3, "Users with different data should not be equal");
        assertNotEquals(user1, user4, "Users with different password should not be equal");
        assertNotEquals(null, user1, "User should not be equal to null");
        assertNotEquals(user1, new Object(), "User should not be equal to an object of a different class");
        assertEquals(user1.hashCode(), user2.hashCode(), "Hashcodes for equal objects must be the same");
    }
}
