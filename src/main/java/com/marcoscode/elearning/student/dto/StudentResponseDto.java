package com.marcoscode.elearning.student.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StudentResponseDto(
        Long id,
        String fullName,
        String email,
        LocalDateTime createdAt,
        int enrolledCourses,
        List<StudentCourseSummaryDto> courseSummariesList
) {
}
