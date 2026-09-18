package com.azue.authservice.config.userDetails;

import com.azue.authservice.domain.entity.User;
import com.azue.authservice.domain.enums.UserStatus;
import com.azue.authservice.repository.AuthRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AuthRepository authRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = authRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),                         // email
                user.getPassword(),                      // password
                user.getStatus() == UserStatus.ACTIVE,   // enabled
                true,                                    // accountNonExpired
                true,                                    // credentialsNonExpired
                user.getStatus() != UserStatus.INACTIVE, // accountNonLocked
                mapAuthorities(user)                     // role + permission
        );
    }


    private Collection<? extends GrantedAuthority> mapAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        // ROLE_
        authorities.add(new SimpleGrantedAuthority(user.getRole().asAuthority()));
        // PERMISSIONS
        user.getRole().getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.name()))
        );
        return authorities;
    }
}
