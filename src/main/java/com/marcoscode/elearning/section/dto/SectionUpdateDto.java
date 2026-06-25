package com.marcoscode.elearning.section.dto;


import jakarta.validation.constraints.Size;

public record SectionUpdateDto(
        @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters if provided")
        String title,
        Integer orderIndex
) {
}
