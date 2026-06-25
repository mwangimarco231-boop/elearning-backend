package com.marcoscode.elearning.lecture.dto;


import com.marcoscode.elearning.resource.dto.ResourceResponseDto;

import java.util.List;

public record LectureResponseDto(
        Long lectureId,
        String title,
        Integer durationSeconds,
        Long sectionId,
        String sectionName,
        List<ResourceResponseDto> resources
) {
}
