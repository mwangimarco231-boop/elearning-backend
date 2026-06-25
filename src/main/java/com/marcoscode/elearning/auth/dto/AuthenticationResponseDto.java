package com.marcoscode.elearning.auth.dto;

public record AuthenticationResponseDto(
        String accessToken,
        String refreshToken
) {
}
