package com.azue.authservice.domain.enums;

public enum Permission {

    // TODO: assess whether the permissions will be linked to roles or if they will be linked directly to the resources to be used by the user.

    // Admin Management
    ADMIN_READ,
    ADMIN_CREATE,
    ADMIN_UPDATE,
    ADMIN_DELETE,
    // User management
    USER_READ,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,
}