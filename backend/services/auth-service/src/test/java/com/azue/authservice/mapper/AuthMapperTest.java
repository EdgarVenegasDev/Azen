package com.azue.authservice.mapper;

import com.azue.authservice.domain.entity.User;
import com.azue.authservice.dto.response.AuthResponse;
import com.azue.authservice.dto.response.RegisterResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthMapperTest {

    private final AuthMapper authMapper = Mappers.getMapper(AuthMapper.class);

    @Test
    void shouldMapUserToAuthResponseAndRegisterResponse() {
        User user = User.create("Jane", "Doe", "jane.doe@example.com", "encoded-password");

        AuthResponse authResponse = authMapper.toResponse(user);
        RegisterResponse registerResponse = authMapper.toRegisterResponse(user);

        assertNotNull(authResponse);
        assertEquals(user.getFirstName(), registerResponse.firstName());
        assertEquals(user.getLastName(), registerResponse.lastName());
        assertEquals(user.getEmail(), registerResponse.email());
        assertEquals(user.getRole().name(), registerResponse.role());
        assertEquals(user.getStatus().name(), registerResponse.status());
    }
}


