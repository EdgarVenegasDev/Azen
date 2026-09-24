package com.azue.authservice.dto.response;

public record RegisterResponse(
        String firstName,
        String lastName,
        String email,
        String role,
        String status) { }