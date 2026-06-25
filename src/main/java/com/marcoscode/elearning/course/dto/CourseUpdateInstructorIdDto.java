package com.marcoscode.elearning.course.dto;

import jakarta.validation.constraints.NotNull;

public record CourseUpdateInstructorIdDto(
        @NotNull(message = "New instructor ID is required")
        Long newInstructorId
) {
}
