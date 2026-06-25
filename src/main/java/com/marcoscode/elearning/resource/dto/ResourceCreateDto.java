package com.marcoscode.elearning.resource.dto;

import com.marcoscode.elearning.resource.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResourceCreateDto(

        @NotBlank(message = "Resource title is required")
        @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
        String title,

        @NotBlank(message = "File URL or content string cannot be blank")
        @Size(max = 2000, message = "Content or URL path cannot exceed 2000 characters")
        String fileUrl,

        @NotNull(message = "Resource type (VIDEO, FILE, or TEXT) is required")
        ResourceType resourceType
) {
}
