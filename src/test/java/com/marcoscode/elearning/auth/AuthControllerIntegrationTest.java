package com.marcoscode.elearning.auth;

import com.marcoscode.elearning.auth.dto.AuthenticationResponseDto;
import com.marcoscode.elearning.auth.dto.LoginRequestDto;
import com.marcoscode.elearning.auth.dto.LogoutRequestDto;
import com.marcoscode.elearning.auth.dto.RefreshTokenRequestDto;
import com.marcoscode.elearning.user.Role;
import com.marcoscode.elearning.user.User;
import com.marcoscode.elearning.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    User user;
    LoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("student@test.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("John")
                .lastName("Doe")
                .role(Role.STUDENT)
                .build();

        userRepository.save(user);

        loginRequest = new LoginRequestDto(
                "student@test.com",
                "password123"
        );
    }

    @Nested
    @DisplayName("User Login Test")
    class LoginTest {

        @Test
        void shouldLoginSuccessfully() throws Exception {

            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(loginRequest))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken")
                            .exists())
                    .andExpect(jsonPath("$.refreshToken")
                            .exists());
        }

        @Test
        void shouldFailLoginWithWrongPassword() throws Exception {

            LoginRequestDto request = new LoginRequestDto(
                    "student@test.com",
                    "wrongPassword"
            );

            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error")
                            .value("Unauthorized"))
                    .andExpect(jsonPath("$.message")
                            .value("The password you entered is incorrect."));
        }

        @Test
        void shouldFailLoginWhenEmailDoesNotExist() throws Exception {

            LoginRequestDto request = new LoginRequestDto(
                    "wrongEmail@test.com",
                    "password123"
            );

            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error")
                            .value("Not Found"));

        }
    }

    @Nested
    @DisplayName("Get current User Test")
    class getCurrentUserTest {

        @Test
        void shouldReturnCurrentUser() throws Exception {
            String loginResponse = mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(loginRequest))
                    )
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            AuthenticationResponseDto authResponse = objectMapper.readValue(
                    loginResponse,
                    AuthenticationResponseDto.class
            );

            mockMvc.perform(
                            get("/api/v1/auth/me")
                                    .header(
                                            "Authorization",
                                            "Bearer " +
                                                    authResponse.accessToken()
                                    )
                    )
                    .andExpect(status().isOk());
        }

        @Test
        void shouldRejectUnauthenticatedUser() throws Exception {

            mockMvc.perform(
                            get("/api/v1/auth/me")
                    )
                    .andExpect(status().isUnauthorized());
        }

    }

    @Nested
    @DisplayName("Refresh token test")
    class refreshTokenTest {

        @Test
        @DisplayName("Should rotate refresh token and return new credentials when token is valid")
        void shouldRefreshTokenSuccessfully() throws Exception {

            String oldTokenString = "valid-integration-test-refresh-token-123";

            RefreshToken seedToken = RefreshToken.builder()
                    .token(oldTokenString)
                    .user(user)
                    .revoked(false)
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .build();
            refreshTokenRepository.save(seedToken);

            RefreshTokenRequestDto requestPayload = new RefreshTokenRequestDto(oldTokenString);

            mockMvc.perform(
                            post("/api/v1/auth/refresh")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestPayload))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.refreshToken").value(org.hamcrest.Matchers.not(oldTokenString)));

            Optional<RefreshToken> updatedTokenOpt = refreshTokenRepository.findByToken(oldTokenString);
            assertTrue(updatedTokenOpt.isPresent());
            assertTrue(updatedTokenOpt.get().isRevoked(), "The old refresh token should be revoked after rotation");
        }

        @Test
        void shouldNotRefreshUsingRevokedToken() throws Exception {

            RefreshToken revokedToken = RefreshToken.builder()
                    .token("valid-integration-test-refresh-token-123")
                    .user(user)
                    .revoked(true)
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .build();

            refreshTokenRepository.save(revokedToken);

            RefreshTokenRequestDto request = new RefreshTokenRequestDto(
                    revokedToken.getToken()
            );

            mockMvc.perform(
                    post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Logout token test")
    class logoutTokenTest {

        @Test
        @DisplayName("Should logout successfully")
        void shouldLogoutSuccessfully() throws Exception {

            RefreshToken refreshToken = RefreshToken.builder()
                    .token("logout-token")
                    .user(user)
                    .revoked(false)
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .build();

            refreshTokenRepository.save(refreshToken);

            LogoutRequestDto request = new LogoutRequestDto(
                    refreshToken.getToken()
            );

            mockMvc.perform(
                    post("/api/v1/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))

            )
                    .andExpect(status().isOk());

            RefreshToken updateToken = refreshTokenRepository
                    .findByToken(refreshToken.getToken())
                    .orElseThrow();

            assertTrue(updateToken.isRevoked());
        }
    }

}
