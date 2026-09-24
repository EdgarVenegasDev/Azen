package com.azue.authservice.domain.entity;

import com.azue.authservice.domain.enums.Role;
import com.azue.authservice.domain.enums.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

    @Test
    void shouldCreateAndMutateUserState() {
        User user = User.create("Jane", "Doe", "jane.doe@example.com", "encoded-password");

        assertEquals("Jane", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("jane.doe@example.com", user.getEmail());
        assertEquals("encoded-password", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());

        user.updateName("Janet", "Smith");
        user.changeEmail("janet.smith@example.com");
        user.changePassword("new-password");
        user.changeRole(Role.ADMIN);
        user.deactivate();
        user.activate();
        user.changeStatus(UserStatus.INACTIVE);

        assertEquals("Janet", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("janet.smith@example.com", user.getEmail());
        assertEquals("new-password", user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals(UserStatus.INACTIVE, user.getStatus());
    }
}

