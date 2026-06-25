package com.marcoscode.elearning.resource.dto;

import com.marcoscode.elearning.resource.ResourceType;

public record ResourceResponseDto(
        Long id,
        String title,
        String fileUrl,
        ResourceType resourceType
) {
}
