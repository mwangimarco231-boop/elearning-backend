package com.marcoscode.elearning.course.dto;

import com.marcoscode.elearning.course.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;

public record CourseUpdateDto(
        @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
        String title,

        Level courseLevel,

        @Positive(message = "Price must be a positive number")
        Double price
) {
}
