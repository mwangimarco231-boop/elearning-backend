package com.marcoscode.elearning.auth.dto;


public record CurrentUserRepositoryDto(
        Long id,
        String email,
        String role
) {
}
