package com.marcoscode.elearning.instructor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record InstructorUpdateDto(
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @Email(message = "Please provide a valid email address")
        @Size(max = 255, message = "Email cannot exceed 255 characters")
        String email,

        @Size(max = 1000, message = "Biography cannot exceed 1000 characters")
        String bio
) {
}
