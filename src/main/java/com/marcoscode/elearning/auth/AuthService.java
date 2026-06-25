package com.marcoscode.elearning.auth;

import com.marcoscode.elearning.auth.dto.*;
import com.marcoscode.elearning.security.CustomUserDetails;
import com.marcoscode.elearning.security.JwtService;
import com.marcoscode.elearning.security.SecurityService;
import com.marcoscode.elearning.user.User;
import com.marcoscode.elearning.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationResponseDto login(
            LoginRequestDto request
    ){

        if (userRepository.findByEmail(request.email()).isEmpty()) {
            throw new EntityNotFoundException("No user found with email: " + request.email());
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("The password you entered is incorrect.");
        }


        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        String accessToken = jwtService.generateToken(userDetails);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found "));

        String refreshToken = jwtService.generateRefreshToken();

        refreshTokenService.createRefreshToken(
                user,
                refreshToken,
                LocalDateTime.now().plusDays(7)
        );

        return new AuthenticationResponseDto(
                accessToken,
                refreshToken
        );
    }

    public AuthenticationResponseDto refreshToken(
            RefreshTokenRequestDto request
    ) {

        RefreshToken refreshToken = refreshTokenService.getByToken(request.refreshToken());

        if (!refreshTokenService.isValid(refreshToken)) {
            throw new IllegalStateException(
                    "Refresh token is invalid or expired"
            );
        }

        refreshTokenService.revokeToken(refreshToken);

        User user = refreshToken.getUser();

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken = jwtService.generateToken(userDetails);

        String newRefreshToken = jwtService.generateRefreshToken();

        refreshTokenService.createRefreshToken(
                user,
                newRefreshToken,
                LocalDateTime.now().plusDays(7)
        );

        return new AuthenticationResponseDto(
                accessToken,
                newRefreshToken
        );

    }

    public void logout(LogoutRequestDto requestDto) {

        RefreshToken refreshToken = refreshTokenService.getByToken(requestDto.refreshToken());

        refreshTokenService.revokeToken(refreshToken);
    }


    public CurrentUserRepositoryDto getCurrentUser() {
        CustomUserDetails user = securityService.getCurrentUser();

        return new CurrentUserRepositoryDto(
                user.getId(),
                user.getUsername(),
                user.getAuthorities()
                        .stream()
                        .findFirst()
                        .orElseThrow()
                        .getAuthority()
        );
    }
}
