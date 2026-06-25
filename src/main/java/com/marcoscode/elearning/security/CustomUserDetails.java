package com.marcoscode.elearning.security;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;


@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long id;

    private final String username;

    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

}
