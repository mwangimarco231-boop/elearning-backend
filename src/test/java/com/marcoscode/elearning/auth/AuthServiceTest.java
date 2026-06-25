package com.marcoscode.elearning.auth;

import com.marcoscode.elearning.auth.dto.*;
import com.marcoscode.elearning.security.CustomUserDetails;
import com.marcoscode.elearning.security.JwtService;
import com.marcoscode.elearning.security.SecurityService;
import com.marcoscode.elearning.user.User;
import com.marcoscode.elearning.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() {

            LoginRequestDto request =
                    new LoginRequestDto(
                            "john@test.com",
                            "password"
                    );

            User user = User.builder()
                    .id(1L)
                    .email("john@test.com")
                    .build();

            UserDetails userDetails =
                    org.springframework.security.core.userdetails.User
                            .withUsername("john@test.com")
                            .password("password")
                            .authorities("ROLE_STUDENT")
                            .build();

            when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));

            when(userDetailsService.loadUserByUsername("john@test.com")).thenReturn(userDetails);

            when(jwtService.generateToken(userDetails)).thenReturn("access-token");

            when(jwtService.generateRefreshToken()).thenReturn("refresh-token");

            AuthenticationResponseDto result = authService.login(request);

            assertNotNull(result);
            assertEquals("access-token", result.accessToken());
            assertEquals("refresh-token", result.refreshToken());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

            verify(refreshTokenService).createRefreshToken(
                            eq(user),
                            eq("refresh-token"),
                            any(LocalDateTime.class)
                    );
        }

        @Test
        @DisplayName("Should throw when email not found")
        void shouldThrowWhenEmailNotFound() {

            LoginRequestDto request =
                    new LoginRequestDto(
                            "missing@test.com",
                            "password"
                    );

            when(userRepository.findByEmail("missing@test.com"))
                    .thenReturn(Optional.empty());

            EntityNotFoundException exception =
                    assertThrows(
                            EntityNotFoundException.class,
                            () -> authService.login(request)
                    );

            assertEquals(
                    "No user found with email: missing@test.com",
                    exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should throw when password is incorrect")
        void shouldThrowWhenPasswordIncorrect() {

            LoginRequestDto request =
                    new LoginRequestDto(
                            "john@test.com",
                            "wrong-password"
                    );

            User user = User.builder()
                    .email("john@test.com")
                    .build();

            when(userRepository.findByEmail("john@test.com"))
                    .thenReturn(Optional.of(user));

            doThrow(new BadCredentialsException("bad credentials"))
                    .when(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));

            BadCredentialsException exception =
                    assertThrows(
                            BadCredentialsException.class,
                            () -> authService.login(request)
                    );

            assertEquals(
                    "The password you entered is incorrect.",
                    exception.getMessage()
            );
        }
    }

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() {

            User user = User.builder()
                    .id(1L)
                    .email("john@test.com")
                    .build();

            RefreshToken refreshToken = RefreshToken.builder()
                    .token("old-refresh")
                    .user(user)
                    .build();

            RefreshTokenRequestDto request =
                    new RefreshTokenRequestDto("old-refresh");

            UserDetails userDetails =
                    org.springframework.security.core.userdetails.User
                            .withUsername("john@test.com")
                            .password("password")
                            .authorities("ROLE_STUDENT")
                            .build();

            when(refreshTokenService.getByToken("old-refresh"))
                    .thenReturn(refreshToken);

            when(refreshTokenService.isValid(refreshToken))
                    .thenReturn(true);

            when(userDetailsService.loadUserByUsername("john@test.com"))
                    .thenReturn(userDetails);

            when(jwtService.generateToken(userDetails))
                    .thenReturn("new-access");

            when(jwtService.generateRefreshToken())
                    .thenReturn("new-refresh");

            AuthenticationResponseDto result =
                    authService.refreshToken(request);

            assertEquals("new-access", result.accessToken());
            assertEquals("new-refresh", result.refreshToken());

            verify(refreshTokenService)
                    .revokeToken(refreshToken);

            verify(refreshTokenService)
                    .createRefreshToken(
                            eq(user),
                            eq("new-refresh"),
                            any(LocalDateTime.class)
                    );
        }

        @Test
        @DisplayName("Should throw when refresh token is invalid")
        void shouldThrowWhenRefreshTokenInvalid() {

            RefreshToken refreshToken =
                    RefreshToken.builder().build();

            RefreshTokenRequestDto request =
                    new RefreshTokenRequestDto("expired");

            when(refreshTokenService.getByToken("expired"))
                    .thenReturn(refreshToken);

            when(refreshTokenService.isValid(refreshToken))
                    .thenReturn(false);

            IllegalStateException exception =
                    assertThrows(
                            IllegalStateException.class,
                            () -> authService.refreshToken(request)
                    );

            assertEquals(
                    "Refresh token is invalid or expired",
                    exception.getMessage()
            );
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should revoke refresh token")
        void shouldLogoutSuccessfully() {

            RefreshToken refreshToken =
                    RefreshToken.builder().build();

            LogoutRequestDto request =
                    new LogoutRequestDto("refresh-token");

            when(refreshTokenService.getByToken("refresh-token"))
                    .thenReturn(refreshToken);

            assertDoesNotThrow(
                    () -> authService.logout(request)
            );

            verify(refreshTokenService)
                    .revokeToken(refreshToken);
        }
    }

    @Nested
    @DisplayName("Current User Tests")
    class CurrentUserTests {

        @Test
        @DisplayName("Should return current user information")
        void shouldReturnCurrentUser() {

            CustomUserDetails user =
                    mock(CustomUserDetails.class);

            when(user.getId()).thenReturn(1L);
            when(user.getUsername()).thenReturn("john@test.com");


            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
            doReturn(authorities).when(user).getAuthorities();

            when(securityService.getCurrentUser())
                    .thenReturn(user);

            CurrentUserRepositoryDto result =
                    authService.getCurrentUser();

            assertEquals(1L, result.id());
            assertEquals("john@test.com", result.email());
            assertEquals("ROLE_STUDENT", result.role());
        }
    }
}