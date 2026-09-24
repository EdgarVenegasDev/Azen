package com.azue.authservice.config.userDetails;

import com.azue.authservice.domain.entity.User;
import com.azue.authservice.domain.enums.Permission;
import com.azue.authservice.domain.enums.Role;
import com.azue.authservice.domain.enums.UserStatus;
import com.azue.authservice.repository.AuthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    private static final String EMAIL = "jane.doe@example.com";

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsernameShouldMapAuthoritiesAndActiveFlags() {
        User user = User.create("Jane", "Doe", EMAIL, "encoded-password");
        when(authRepository.findByEmail(EMAIL)).thenReturn(java.util.Optional.of(user));

        var userDetails = userDetailsService.loadUserByUsername(EMAIL);

        assertEquals(EMAIL, userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isAccountNonExpired());

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        Set<String> expectedAuthorities = user.getRole().getPermissions().stream()
                .map(Permission::name)
                .collect(Collectors.toSet());
        expectedAuthorities.add(Role.USER.asAuthority());

        assertEquals(expectedAuthorities, authorities);
    }

    @Test
    void loadUserByUsernameShouldDisableInactiveUsers() {
        User user = User.create("Jane", "Doe", EMAIL, "encoded-password");
        user.changeStatus(UserStatus.INACTIVE);
        when(authRepository.findByEmail(EMAIL)).thenReturn(java.util.Optional.of(user));

        var userDetails = userDetailsService.loadUserByUsername(EMAIL);

        assertFalse(userDetails.isEnabled());
        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    void loadUserByUsernameShouldFailWhenUserDoesNotExist() {
        when(authRepository.findByEmail(EMAIL)).thenReturn(java.util.Optional.empty());

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(EMAIL));
    }
}
