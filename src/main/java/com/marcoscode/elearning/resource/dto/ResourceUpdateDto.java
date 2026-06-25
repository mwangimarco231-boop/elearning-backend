package com.marcoscode.elearning.resource.dto;

import com.marcoscode.elearning.resource.ResourceType;
import jakarta.validation.constraints.Size;

public record ResourceUpdateDto(

        @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
        String title,

        @Size(max = 2000, message = "Content or URL path cannot exceed 2000 characters")
        String fileUrl,

        ResourceType resourceType
) {
}
