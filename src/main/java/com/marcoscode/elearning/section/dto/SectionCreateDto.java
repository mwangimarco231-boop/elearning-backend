package com.marcoscode.elearning.section.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SectionCreateDto(

        @NotBlank(message = "Section title is required")
        @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
        String title,

        @NotNull(message = "Order index is required")
        @PositiveOrZero(message = "Order index must be zero or a positive number")
        Integer orderIndex
) {
}
