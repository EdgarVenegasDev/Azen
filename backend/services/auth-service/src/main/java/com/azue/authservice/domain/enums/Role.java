package com.azue.authservice.domain.enums;

import lombok.Getter;

import java.util.Set;

@Getter
public enum Role {

    // TODO: evaluate the roles and structure to be used for access to system resources.
    ADMIN(Set.of(
            Permission.ADMIN_READ,
            Permission.ADMIN_CREATE,
            Permission.ADMIN_UPDATE,
            Permission.ADMIN_DELETE
    )),

    USER(Set.of(
            Permission.USER_READ,
            Permission.USER_CREATE,
            Permission.USER_UPDATE,
            Permission.USER_DELETE
    ));

    //STRUCT TO SECURITY WITH SPRING BOOT SECURITY
    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
