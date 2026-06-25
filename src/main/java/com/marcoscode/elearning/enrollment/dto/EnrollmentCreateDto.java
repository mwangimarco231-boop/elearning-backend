package com.marcoscode.elearning.enrollment.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollmentCreateDto(
        @NotNull(message = "Course id is required")
        Long courseId
) {
}
