package com.marcoscode.elearning.course.dto;

import com.marcoscode.elearning.course.Level;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseCreateDto(
        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
        String title,

        @NotNull(message = "Price is required")
        Double price,

        @NotNull(message = "Course Level is required")
        @Enumerated(EnumType.STRING)
        Level courseLevel,

        Long instructorId
) {
}
