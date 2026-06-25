package com.marcoscode.elearning.lecture.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LectureCreateDto(
        @NotBlank(message = "Lecture title is required")
        @Size(min = 2, max = 150, message = "Title must be between 2 and 150 characters")
        String title,

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be at least 1 second")
        Integer durationSeconds
) {
}
