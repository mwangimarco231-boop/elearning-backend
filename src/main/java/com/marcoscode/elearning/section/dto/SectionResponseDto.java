package com.marcoscode.elearning.section.dto;

public record SectionResponseDto(
        Long sectionId,
        String title,
        Integer orderIndex,
        Long courseId,
        String courseName
) {
}
